shared/shared-domain/
│
├── build.gradle.kts
│
└── src/main/java/com/platform/domain/
│
├── common/                              ── DDD Building Blocks ──
│   ├── AggregateRoot.java               (marker interface)
│   ├── DomainEntity.java                (marker interface)
│   ├── ValueObject.java                 (marker interface)
│   └── DomainEvent.java                 (marker interface)
│
├── testcase/                            ── Bounded Context: Test Case ──
│   ├── TestCaseId.java                  (Value Object — identity)
│   ├── TestCase.java                    (Aggregate Root)
│   ├── GherkinStep.java                 (Value Object)
│   ├── StepAssertion.java               (Value Object)
│   ├── VariableExtraction.java          (Value Object)
│   ├── TestParameter.java               (Value Object)
│   └── TestCaseRepository.java          (Port — interface)
│
├── testsuite/                           ── Bounded Context: Test Suite ──
│   ├── TestSuiteId.java                 (Value Object — identity)
│   ├── TestSuite.java                   (Aggregate Root)
│   └── TestSuiteRepository.java         (Port — interface)
│
├── pageobject/                          ── Bounded Context: Page Object ──
│   ├── PageObjectId.java                (Value Object — identity)
│   ├── PageObject.java                  (Aggregate Root)
│   ├── PageElement.java                 (Entity)
│   ├── ElementLocator.java              (Value Object)
│   └── PageObjectRepository.java        (Port — interface)
│
├── environment/                         ── Bounded Context: Environment ──
│   ├── EnvironmentId.java               (Value Object — identity)
│   ├── Environment.java                 (Aggregate Root)
│   └── EnvironmentRepository.java       (Port — interface)
│
├── execution/                           ── Bounded Context: Execution ──
│   ├── ExecutionTargetId.java           (Value Object — identity)
│   ├── ExecutionTarget.java             (Value Object — immutable config)
│   ├── TestRunId.java                   (Value Object — identity)
│   ├── TestRun.java                     (Aggregate Root)
│   ├── TestCaseResult.java              (Entity)
│   ├── TestStepResult.java              (Value Object)
│   ├── TestRunRepository.java           (Port — interface)
│   └── ExecutionTargetRepository.java   (Port — interface)
│
├── project/                             ── Bounded Context: Project ──
│   ├── ProjectId.java                   (Value Object — identity)
│   ├── Project.java                     (Aggregate Root)
│   └── ProjectRepository.java           (Port — interface)
│
└── enums/                               ── Shared Enums ──
├── ActionType.java
├── AssertionType.java
├── BrowserType.java
├── ComparisonOperator.java
├── ExtractionSource.java
├── GherkinKeyword.java
├── LocatorStrategy.java
├── OSType.java
├── PlatformType.java
├── Priority.java
├── RunStatus.java
└── TestType.java