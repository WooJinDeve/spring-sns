### 대용량 서비스를 구축하기 위해서 알아야하는 것들

- 가장 중요한 것은 `데이터`이다.
- 어떻게 많은 양의 데이트를 안정적으로 삽입, 갱신, 조회 할것인지!
    - **정규화**, **인덱스**, **트랜잭션**, **동시성 제어** 등..

### 실습 환경 구축

```gradle
 dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-jdbc'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.data:spring-data-commons:2.7.2'
	implementation 'org.springdoc:springdoc-openapi-ui:1.6.8'

	compileOnly 'org.projectlombok:lombok'
	developmentOnly 'org.springframework.boot:spring-boot-devtools'
	runtimeOnly 'mysql:mysql-connector-java'

	annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
	annotationProcessor 'org.projectlombok:lombok'

	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testImplementation 'org.jeasy:easy-random-core:5.0.0'
}
```