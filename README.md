# Kalix Workshop - Loan application - Scala

## Prerequisite
Java 11 or later<br>
SBT 1.3.6 or later<br>
[Kalix CLI](https://docs.kalix.io/kalix/install-kalix.html) <br>
Docker 20.10.8 or higher (client and daemon)<br>
Container registry with public access (like Docker Hub)<br>
Access to the `gcr.io/kalix-public` container registry<br>
cURL<br>
IDE / editor<br>

## Create kickstart sbt project using Giter8

```
sbt new lightbend/kalix-value-entity.g8
```

name [My Kalix Project]: `loan-application`<br>
sdk_version [1.0.1]:<br>
sbt_version [1.6.2]:<br>
scala_version [2.13.7]:<br>
package [com.example]: `io.kx.loanapp`<br>

## Import generated project in your IDE/editor
<i><b>Delete all proto files after done</b></i>

## Update main class
In `build.sbt` add:
1. `mainClass := Option("io")`
2. `version := "1.0-SNAPSHOT`


# Loan application service

## Define API data structure and endpoints (GRPC)
Create `io/kx/loanapp/api` folder in `src/main/proto` folder. <br>
Create `loan_app_api.proto` in `src/main/proto/io/kx/loanapp/api` folder. <br>
Create: <br>
- headers
- state
- commands
- service

<i><b>Tip</b></i>: Check content in `step-1` git branch

## Define persistence (domain) data structure  (GRPC)
Create `io/kx/loanapp/doman` folder in `src/main/proto` folder. <br>
Create `loan_app_domain.proto` in `src/main/proto/io/kx/loanapp/domain` folder. <br>
Create: <br>
- headers
- state
- events

<i><b>Tip</b></i>: Check content in `step-1` git branch
## Add codegen annotations in API data structure and endpoints (GRPC)
In `src/main/proto/io/kx/loanapp/api/loan_app_api.proto` add AkkaServerless codegen annotations to GRPC service
```
service LoanAppService {
```
```
option (kalix.codegen) = {
    event_sourced_entity: {
      name: "io.kx.loanapp.domain.LoanAppEntity"
      entity_type: "loanapp"
      state: "io.kx.loanapp.domain.LoanAppDomainState"
      events: [
        "io.kx.loanapp.domain.Submitted",
        "io.kx.loanapp.domain.Approved",
        "io.kx.loanapp.domain.Declined"
      ]
    }
  };
```
```
...
```
<i><b>Note</b></i>: `event_sourced_entity.name` has to be a unique name
## Compile maven project to trigger codegen
```
sbt compile
```
Compile will generate help classes and these skeleton classes<br><br>
Business logic:<br>
`src/main/scala/io/Main`<br>
`src/main/scala/io/kx/loanapp/domain/LoanAppEntity`<br>
<br>
Unit tests:<br>
`src/test/scala/io/kx/loanapp/domain/LoanAppEntitySpec`<br>
Integration tests:<br>
`src/test/scala/io/kx/loanapp/api/LoanAppServiceIntegrationSpec`<br>

## Implement entity skeleton class
Implement `src/main/scala/io/kx/loanapp/domain/LoanAppEntity` class <br>
<i><b>Tip</b></i>: Check content in `step-1` git branch

## Implement unit test
Implement  `src/test/scala/io/kx/loanapp/domain/LoanAppEntitySpec` class<br>
<i><b>Tip</b></i>: Check content in `step-1` git branch

## Implement integration test
Implement `src/test/scala/io/kx/loanapp/api/LoanAppServiceIntegrationSpec` class<br>
<i><b>Tip</b></i>: Check content in `step-1` git branch

## Run unit and integration tests
```
sbt test
```
<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.

## Run locally

In project root folder there is `docker-compose.yaml` for running `kalix proxy` and (optionally) `google pubsub emulator`.
<i><b>Tip</b></i>: If you do not require google pubsub emulator then comment it out in `docker-compose.yaml`
```
docker-compose up
```

Start the service:

```
sbt compile run
```

## Test service locally
Submit loan application:
```
curl -XPOST -d '{
  "client_id": "12345",
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' http://localhost:9000/loanapp/1 -H "Content-Type: application/json"
```

Get loan application:
```
curl -XGET http://localhost:9000/loanapp/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPUT http://localhost:9000/loanapp/1/approve -H "Content-Type: application/json"
```

## Package & Deploy

```
sbt docker:publish -Ddocker.username=<dockerId>
```
<i><b>Note</b></i>: Replace `<dockerId>` with required dockerId

## Register for Kalix account or Login with existing account
[Register](https://console.kalix.io/register)

## kalix CLI
Validate version:
```
kalix version
```
Login (need to be logged in the Kalix Console in web browser):
```
kalix auth login
```
Create new project:
```
kalix projects new loan-application --region <REGION>
```
<i><b>Note</b></i>: Replace `<REGION>` with desired region

List projects:
```
kalix projects list
```
Set project:
```
kalix config set project loan-application
```
## Deploy service
```
kalix service deploy loan-application my-docker-repo/loan-application:1.0-SNAPSHOT
```
<i><b>Note</b></i>: Replace `my-docker-repo` with your docker repository

List services:
```
kalix services list
```
```
NAME               AGE    REPLICAS   STATUS   DESCRIPTION   
loan-application   102s   1          Ready  
```
## Expose service
```
kalix services expose loan-application
```
Result:
`
Service 'loan-application' was successfully exposed at: lingering-morning-1201.us-east1.kalix.app
`
## Test service in production
Submit loan application:
```
curl -XPOST -d '{
  "client_id": "12345",
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' https://lingering-morning-1201.us-east1.kalix.app/loanapp/1 -H "Content-Type: application/json"
```
Get loan application:
```
curl -XGET https://lingering-morning-1201.us-east1.kalix.app/loanapp/1 -H "Content-Type: application/json"
```
Approve:
```
curl -XPUT https://lingering-morning-1201.us-east1.kalix.app/loanapp/1/approve -H "Content-Type: application/json"
```