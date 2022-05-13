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

## Package & Publish

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
# Loan application processing service

## Increment version
In `build.sbt` set `version` to `1.1-SNAPSHOT`

## Define API data structure and endpoints (GRPC)
Create `io/kx/loanproc/api` folder in `src/main/proto` folder. <br>
Create `loan_proc_api.proto` in `src/main/proto/io/kx/loanproc/api` folder. <br>
Create: <br>
- state
- commands
- service

<i><b>Tip</b></i>: Check content in `step-2` git branch

## Define persistence (domain) data structure  (GRPC)
Create `io/kx/loanproc/domain` folder in `src/main/proto` folder. <br>
Create `loan_proc_domain.proto` in `src/main/proto/io/kx/loanproc/domain` folder. <br>
Create: <br>
- state
- events

<i><b>Tip</b></i>: Check content in `step-2` git branch
## Add codegen annotations in API data structure and endpoints (GRPC)
In `src/main/proto/io/kx/loanproc/api/loan_proc_api.proto` add AkkaServerless codegen annotations to GRPC service
```
service LoanProcService {
```
```
option (kalix.codegen) = {
    event_sourced_entity: {
      name: "io.kx.loanproc.domain.LoanProcEntity"
      entity_type: "loanproc"
      state: "io.kx.loanproc.domain.LoanProcDomainState"
      events: [
        "io.kx.loanproc.domain.ProcessStarted",
        "io.kx.loanproc.domain.Approved",
        "io.kx.loanproc.domain.Declined"
      ]
    }
  };
```
```
...
```
<i><b>Note</b></i>: `event_sourced_entity.name` has to be a unique name
## Compile sbt project to trigger codegen
```
sbt compile
```

Compile will generate these skeleton classes<br><br>
Business logic:<br>
`src/main/scala/io/kx/loanproc/domain/LoanProcEntity`<br>
<br>
Unit tests:<br>
`src/test/scala/io/kx/loanproc/domain/LoanProcEntityTest`<br>
Integration tests:<br>
`src/test/scala/io/kx/loanproc/api/LoanProcEntityIntegrationTest`<br>

## Update Main class
In `src/main/scala/io/Main` you need to add new entity component (`LoanProcEntity`):
```
 KalixFactory.withComponents(
      new LoanAppEntity(_),new LoanProcEntity(_))
```
## Implement entity skeleton class
Implement `src/main/scala/io/kx/loanproc/domain/LoanProcEntity` class<br>
<i><b>Tip</b></i>: Check content in `step-2` git branch

## Implement unit test
Implement `src/test/scala/io/kx/loanproc/domain/LoanProcEntitySpec` class<br>
<i><b>Tip</b></i>: Check content in `step-2` git branch

## Implement integration test
Implement `src/test/scala/io/kx/loanproc/api/LoanProcServiceIntegrationSpec` class<br>
<i><b>Tip</b></i>: Check content in `step-2` git branch

## Run unit & integration test
```
sbt test
```
<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.
## Package & Publish
```
sbt docker:publish -Ddocker.username=<dockerId> 
```
<i><b>Note</b></i>: Replace `<dockerId>` with required dockerId
## Deploy service
```
kalix service deploy loan-application my-docker-repo/loan-application:1.1-SNAPSHOT
```
<i><b>Note</b></i>: Replace `my-docker-repo` with your docker repository
## Test service in production
Start processing:
```
curl -XPOST -d '{
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' https://lingering-morning-1201.us-east1.kalix.app/loanproc/1 -H "Content-Type: application/json"
```

Get loan processing:
```
curl -XGET https://lingering-morning-1201.us-east1.kalix.app/loanproc/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPUT https://lingering-morning-1201.us-east1.kalix.app/loanproc/1/approve -H "Content-Type: application/json"
```

## Loan Process Views

## Increment version
In `build.sbt` set `version` to `1.2-SNAPSHOT`

## Create a view
Create `io/kx/loanproc/view` folder in `src/main/proto` folder. <br>
Create `loan_proc_by_status_view.proto` in `src/main/proto/io/kx/loanproc/view` folder. <br>
Create: <br>
- state
- request/response
- service

<i><b>Note</b></i>: `SELECT` result alias `AS results` needs to correspond with `GetLoanProcByStatusResponse` parameter name `repeated LoanProcViewState results`<br>
<i><b>Note</b></i>: Currently `enums` are not supported as query parameters ([issue 1141](https://github.com/lightbend/kalix-proxy/issues/1141)) so enum `number` value is used for query<br>
<i><b>Tip</b></i>: Check content in `step-3` git branch

## Compile maven project to trigger codegen for views
```
sbt compile
```
Compile will generate help classes (`target/generated-*` folders) and skeleton classes<br><br>

`src/main/scala/io/kx/loanproc/view/LoanProcByStatusView`<br>

In `src/main/scala/io/Main` you need to add view (`LoanProcByStatusView`) initialization:
```
    KalixFactory.withComponents(
      new LoanAppEntity(_),new LoanProcEntity(_), new LoanProcByStatusView(_))
```

## Implement view LoanProcByStatusView skeleton class
Implement `src/main/scala/io/kx/loanproc/view/LoanProcByStatusView` class<br>
<i><b>Tip</b></i>: Check content in `step-3` git branch

##Unit test

Because of the nature of views only Integration tests are done.

## Create integration tests for view
1. Copy `io/kx/loanproc/api/LoanProcServiceIntegrationSpec` class to `io/kx/loanproc/api/LoanProcServiceViewIntegrationSpec`
2. Remove all tests in 
3. Add next to `clien declaration`:
```
private val view = testKit.getGrpcClient(classOf[LoanProcByStatus])
```
5. Add `view test`

<i><b>Tip</b></i>: Check content in `step-3` git branch

## Run integration test
```
sbt test
```
<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.
## Package & Publish
```
sbt docker:publish -Ddocker.username=<dockerId> 
```
<i><b>Note</b></i>: Replace `<dockerId>` with required dockerId
## Deploy service
```
kalix service deploy loan-application my-docker-repo/loan-application:1.2-SNAPSHOT
```
<i><b>Note</b></i>: Replace `my-docker-repo` with your docker repository
## Test service in production
Get loan processing by status:
```
curl -XPOST -d {"status_id":2} https://lingering-morning-1201.us-east1.kalix.app/loanproc/views/by-status -H "Content-Type: application/json"
```