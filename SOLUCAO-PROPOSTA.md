# Solução proposta

### Análise do problema

Para identificar a causa da lentidão os seguintes pontos devem ser analisados: utilização de cpu e memória da aplicação, utilização de cpu e memória da base de 
dados, tamanho do pool de conexões do banco, utilização do pool de conexões do banco, número de queries executadas na base, plano de execução das queries. 

### Possíveis melhorias

* Caso o problema esteja na *utilização de cpu e memória da aplicação*, o caminho mais simples é escalar a aplicação verticalmente, ou seja, 
aumentar os recursos disponíveis de hardware (mais memória ram, mais processadores). Não sendo possível escalar verticalmente, a aplicação deve ser preparada 
para escalar horizontalmente, ou seja, a aplicação deve ser executada de forma distribuída, assim, mais máquinas / nós podem ser adicionados ao cluster conforme 
houver necessidade. 

* Estando o problema na *utilização de cpu e memória da base de dados*, deve-se verificar se a aplicação está utilizado a base de dados de forma adequada. 
Os seguintes pontos devem avaliados: 
    
    * plano de execução das queries: As queries podem estar gastando muito mais recursos do que o necessário devido à falta de indices na base. 
    Ex: Para fazer uma busca de todas as vendas com status 'PENDENTE' o SGBD (sistema gerenciador de banco de dados) deverá varrer todos os registros de venda 
    existentes para determinar se a compra está pendente ou não. Criar um indice no campo status da tabela de vendas reduzirá drasticamente o custo da consulta. 
    
    * número de registros nas tabelas: Tabelas com muitos registros, geram consultas mais lentas. Deve-se verificar a possibilidade de movimentação de dados históricos 
    para outra base de dados. 
    Ex: Poderia ser criada uma rotina de arquivamento dos registros das vendas feitas à mais de seis meses. Os registros poderiam ser excluídos da tabela principal 
    e inseridos em uma tabela de histórico
    
    * número de consultas realizadas: Caso a aplicação consulte uma informação da base com muita frequência, esta informação pode ser armazenada em memória (cache) para 
    diminuir o número de acessos à base. 
    
    * relatórios: uma aplicação que disponibiliza muitos relatórios aos usuários (ex: número de vendas do mês de março, lucro anual, etc) pode alocar muitos recursos da base 
    de dados. Neste cenário seria necessário executar estas operações em uma réplica da base utilizada como somente leitura, ou poderiam ser criadas rotinas para compilar as 
    informações e salvar os relatórios já consolidados. 
    
    * hardward disponível: Se mesmo após a otimização no acesso aos dados a base continuar com alta utilização, resta escalar a base verticalmente e depois horizontalmente.
    
## Solução implementada

Para atender o sistema com um alto número de acessos foi desenvolvida uma solução com alta escalabilidade e disponibilidade baseada nas tecnologias utilizadas pela 
Netflix (spring cloud). 

A solução está dividida em 3 partes principais:
* Eureka Server - Implementa o conceito de "service discovery". Sempre que uma nova instância de um serviço é iniciada, ela se cadastra no Eureka Server, tornando-se visível 
para os outros serviços.

* API Gatway (Zuul Proxy) - Implementa o conceito de "reverse proxy". O API é responsável por receber todas as requisições e direcioná-las para os respectivos serviços. 
O API Gateway consulta o Eureka server para buscar quais instâncias dos serviços estão disponíveis. 

* Weblogin - Disponibiliza o serviço de login. 

Para que múltiplas instâncias do serviço possam ser executadas de forma transparente ao usuário 
(cada request do usuário pode ser processado por uma instância diferente do serviço) foi implementada uma sessão distribuída (spring-session + spring-data-redis), 
a sessão http do usuário é armazenada em um cache redis distribuído e fica acessível para todos os módulos com acesso ao cache, inclusive módulos diferentes, 
é possível compartilhar a sessão do usuário entre uma instância de weblogin e uma instância de api-gateway, por exemplo.
Para autenticação e autorização foi utilizado o spring security com troca de cookies entre aplicação e navegador. 
A consulta  aos dados do usuário persistidos no postgres é feita com spring JDBC Template (com o Hibernate é mais difícil saber exatamente quais consultas sql serão geradas, 
o uso de recursos nativos do BD fica mais complicado e fazer um ajuste fino das consultas eager / lazy traz muita complexidade ao código).
Para otimizar o número de acessos a base foi utilizada a biblioteca spring-cache + spring-data-redis: com algumas poucas anotações (@CacheConfig, @Cacheable) é possível 
colocar retornos de métodos em cache de forma não invasiva (o código que invoca o método não tem conhecimento da existência do cache). 

## Executando a aplicação

*Pré-requisitos:* Git, Java 8, variável de ambiente JAVA_HOME, docker e docker-compose. 

Em um terminal, executar os seguintes comandos: 

    git clone https://github.com/hiago-b-oliveira/jbd.git
    
    cd jbd
    
    git checkout dev
    
    ./mvnw clean install  dockerfile:build
    
    docker-compose -f docker-compose-runall.yml up
    
A aplicação estará diponível em [http://localhost:8080](http://localhost:8080) e é possível fazer login com o usuário `user`, senha `jbd@2018`.
No endereço [http://localhost:9100/](http://localhost:9100/) é possível visualizar o número de instâncias disponíveis de cada serviço.

Existem ainda algumas urls que dizem bastante sobre a aplicação (urls do spring actuator disponíveis em todos os módulos): 

* [http://localhost:9100/actuator/health](http://lost:9100/actuator/health)
* [http://localhost:9100/actuator/metrics](http://localhost:8080/actuator/metrics)
* [http://localhost:8080/actuator/trace](http://localhost:8080/actuator/metrics)
* [http://localhost:8080/weblogin/actuator/auditevents](http://localhost:8080/weblogin/actuator/auditevents)*

Obs: ao acessar as urls `/weblogin/actuator/*` não é possível determinar que qual das instâncias de weblogin as informações pertencem. 
Obs2: após subir todos os módulos o api-gateway pode levar até 1min30s para registrar todas as instâncias de weblogin. Este atraso ocorre devido 
ao tempo dos "heart beats" do Eureka Server. 
