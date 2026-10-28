MSS301 – Practical Exam 2026 Summer – Sample
Created by traltb@fe.edu.vn
General Information
 
Item	Content
Subject Code	MSS301
Subject Name	Microservices with Spring Cloud
Time	90 minutes (including submission time)
Exam Type	PE – Practical Exam
 
Note: a) Internet connection allowed for 10 minutes during setup at the start. b) Only local machine resources may be used.
 

 
1. Business Context
Build a sponsored post booking platform. A User is a creator with a price list ratePerPost (price per post) and a limit maxDailyPosts (maximum posts/day). A Post is an advertising campaign: an advertiser books numberOfPosts posts with a creator over a period startDate..endDate; the system automatically calculates totalCost = ratePerPost × number of days. A campaign can only be created when the creator exists and is ACTIVE.
 

 
2. Exam Requirements
Develop 3 microservices using Spring Boot, Spring Cloud, and related libraries:
 
●User Service
●Post Service
●Media Gateway
 

 
2.1 User Service
 
Item	Content
Project Name	<StudentID>UserService
Requirements	Spring Boot 4.1.0 MVC, Spring JPA, MS SQL Server. Layered Architecture (Controller → Service → Repository).
 
Note: User Service does NOT use OpenFeign (only Post Service calls Feign to User).
 
2.1.1 Configuration for application.properties
 
#	Item	Value
1	spring.datasource.password	sa
2	spring.datasource.username	sa
3	spring.datasource.url	jdbc:sqlserver://localhost:1433;databaseName=MSS301_2026_PE_MEDIA;encrypt=false;
–	spring.jpa.hibernate.ddl-auto	none
4	server.port	8081
5	Context path	default
6	Database name	MSS301_2026_PE_MEDIA
 
2.1.2 Service Specification
2.1.2.1 DTOs
ApiResponse
 
#	Attribute	Type	Description
1	status	int	
2	message	String	Error message
3	data	Object	null / an object / list of objects
4	timestamp	String	Format YYYY-MM-DDThh:mm:ssZ (ISO 8601)
 
UserDTO
 
#	Attribute	Type	Description
1	userId	long	
2	username	String	Unique, max 30
3	email	String	Valid, max 100
4	role	String	USER, CREATOR, ADMIN
5	ratePerPost	BigDecimal	Must be > 0
6	maxDailyPosts	int	Maximum posts/day (1–10)
7	level	int	Creator level (≥ 1)
8	status	String	ACTIVE, SUSPENDED, DEACTIVATED
 
PageDTO
 
#	Attribute	Type	Description
1	size	int	
2	page	int	
3	totalPages	int	
4	totalElements	long	
5	first	boolean	True if first page
6	last	boolean	True if last page
7	content	Object	Is List<UserDTO>
 
Data Structure sample:
 
         
    ApiResponse: { "status": 1, "message": "Successful", "data": {JSON}, "timestamp": "2026-07-24T02:21:45Z" }    
         
    UserDTO: { "userId": 1, "username": "creator_a", "email": "a@example.com", "role": "CREATOR",    
      "ratePerPost": 500000, "maxDailyPosts": 3, "level": 1, "status": "ACTIVE" }    
         
    PageDTO: { "size": 10, "page": 0, "totalPages": 4, "totalElements": 40,    
      "first": true, "last": false, "content": [] }    
         
 
2.1.2.2 API Endpoints — Controller Class: <StudentID>UserController
1. Create User — POST /api/users
●Request Body: UserDTO. Default status = ACTIVE.
 
HTTP Code	Status	Description
201	1	Created successfully. Returns UserDTO. Default status ACTIVE.
406	2	Validation failed. No data returned.
226	3	Duplicate username. No data returned.
500	0	Server error.
 
2. Update User — PUT /api/users/{userId}
●Path Param: userId (long); Request Body: UserDTO; partial update.
 
HTTP Code	Status	Description
200	1	Updated successfully. Returns UserDTO.
406	2	Validation failed.
226	3	Duplicate username.
404	4	User not found.
500	0	Server error.
 
3. Get User Detail — GET /api/users/{userId}
 
HTTP Code	Status	Description
200	1	Returns UserDTO.
404	4	User not found.
500	0	Server error.
 
4. Deactivate User (delete) — DELETE /api/users/{userId}
●Set status = DEACTIVATED. A deactivated user cannot receive campaigns.
 
HTTP Code	Status	Description
200	1	Set to DEACTIVATED successfully. No data returned.
404	4	User not found.
500	0	Server error.
 
5. Get User List — GET /api/users
 
Query Parameters:
 
Parameter	Type	Required	Description
page	Integer	No	Page number (default 0)
size	Integer	No	Page size (default 10, max 100)
role	Enum	No	Filter by role: USER, CREATOR, ADMIN
status	Enum	No	Filter by status: ACTIVE, SUSPENDED, DEACTIVATED
 
HTTP Code	Status	Description
200	1	Returns list of UserDTO in PageDTO.
406	2	Validation failed (size > 100).
500	0	Server error.
 
2.1.3 Database Structure — Table users
 
#	Field	Type	Max Len	Mandatory	Entity Type	Description
1	user_id	Identity	NA	Yes	long	Auto increment
2	username	Nvarchar	30	Yes	String	Unique
3	email	Nvarchar	100	Yes	String	Valid email format
4	role	Nvarchar	15	Yes	Enum Role	USER, CREATOR, ADMIN. Entity uses Enum (@Enumerated(EnumType.STRING)); DB column remains Nvarchar
5	rate_per_post	Decimal(18,2)		Yes	BigDecimal	Must be > 0
6	max_daily_posts	int		Yes	int	1–10 inclusive
7	level	int		Yes	int	≥ 1
8	status	Nvarchar	15	Yes	Enum UserStatus	ACTIVE, SUSPENDED, DEACTIVATED. Entity uses Enum (@Enumerated(EnumType.STRING)); DB column remains Nvarchar
 
Script: create_users_sql.txt.
 

 
2.2 Post Service
 
Item	Content
Project Name	<StudentID>PostService
Requirements	Spring Boot 4.1.0 MVC, OpenFeign, Spring JPA, MS SQL Server. Calls UserService via OpenFeign.
 
2.2.1 Configuration for application.properties
Same as UserService, except server.port = 8082. ddl-auto = none. DB MSS301_2026_PE_MEDIA. Add property user.service.url=http://localhost:8081.
 
2.2.2 Service Specification
2.2.2.1 DTOs
ApiResponse — same structure as ApiResponse in User Service (includes timestamp).
 
PostDTO
 
#	Attribute	Type	Description
1	postId	Long	
2	advertiserName	String	max 100
3	advertiserEmail	String	Valid, max 100
4	advertiserPhone	String	max 20
5	startDate	java.util.Date	No time component. Format dd/MM/yyyy
6	endDate	java.util.Date	No time component. Format dd/MM/yyyy. Must be after startDate
7	numberOfPosts	int	≥ 1, must not exceed the user's maxDailyPosts
8	totalCost	BigDecimal	Calculated = ratePerPost × number of days
9	status	String	DRAFT, SCHEDULED, PUBLISHED, CANCELLED
10	userId	Long	Existence & ACTIVE status checked via UserService
 
PostDetailDTO — same as PostDTO but replaces userId with the user object:
 
#	Attribute	Type	Description
1	postId	long	
2	advertiserName	String	
3	advertiserEmail	String	
4	advertiserPhone	String	
5	startDate	java.util.Date	dd/MM/yyyy
6	endDate	java.util.Date	dd/MM/yyyy
7	numberOfPosts	int	
8	totalCost	BigDecimal	
9	status	String	DRAFT, SCHEDULED, PUBLISHED, CANCELLED
10	user	UserDTO	Retrieved from UserService (via Feign)
 
PageDTO (Post) — same structure as PageDTO in User Service, content = List<PostDetailDTO>.
 
2.2.2.2 API Endpoints — Controller Class: <StudentID>PostController
1. Create Post — POST /api/posts
●Request Body: PostDTO. The user must exist and have status ACTIVE. Default status = SCHEDULED.
 
HTTP Code	Status	Description
201	1	Created successfully. Returns PostDTO. Default SCHEDULED. totalCost auto-calculated.
406	2	Validation failed (date, email, numberOfPosts > maxDailyPosts...).
404	4	userId not found.
400	5	User is not ACTIVE.
500	0	Server error.
 
2. Update Post — PUT /api/posts/{postId}
●Partial update; recalculates totalCost based on the user's current rate.
 
HTTP Code	Status	Description
200	1	Updated successfully. Returns PostDTO.
406	2	Validation failed (endDate ≤ startDate).
404	4	postId not found.
500	0	Server error.
 
3. Get Post Detail — GET /api/posts/{postId}
 
HTTP Code	Status	Description
200	1	Returns PostDetailDTO (includes user object).
404	4	Not found.
500	0	Server error.
 
4. Cancel Post — DELETE /api/posts/{postId}
●Set status = CANCELLED.
 
HTTP Code	Status	Description
200	1	Cancelled successfully. No data returned.
404	4	Not found.
500	0	Server error.
 
5. Get Post List — GET /api/posts
 
Query Parameters:
 
Parameter	Type	Required	Description
page	Integer	No	Page number (default 0)
size	Integer	No	Page size (default 10, max 100)
advertiserName	String	No	Filter by advertiserName (partial match)
status	Enum	No	Filter by status
 
HTTP Code	Status	Description
200	1	Returns list of PostDetailDTO in PageDTO.
406	2	Validation failed (size > 100).
500	0	Server error.
 
2.2.3 Database Structure — Table posts
 
#	Field	Type	Max Len	Mandatory	Entity Type	Description
1	post_id	Identity		Yes	long	Auto generated
2	advertiser_name	Nvarchar	100	Yes	String	
3	advertiser_email	Nvarchar	100	Yes	String	
4	advertiser_phone	Nvarchar	20	Yes	String	
5	start_date	Date	NA	Yes	java.util.Date	
6	end_date	Date	NA	Yes	java.util.Date	Must be after start_date
7	number_of_posts	int	NA	Yes	int	≥ 1 (≤ maxDailyPosts)
8	total_cost	Decimal(18,2)			BigDecimal	≥ 0
9	status	Nvarchar	15	Yes	Enum PostStatus	DRAFT, SCHEDULED, PUBLISHED, CANCELLED. Entity uses Enum (@Enumerated(EnumType.STRING)); DB column remains Nvarchar
10	user_id	int		Yes	Long	Foreign key → users(user_id)
 
Script: create_posts_sql.txt.
 

 
2.3 Media Gateway
 
Item	Content
Project Name	<StudentID>MediaGateway
 
Requirements:
1. Spring Boot 4.1.0, Spring Cloud Gateway 2025.1 (WebFlux).
2. Gateway is the single entry point to the microservices (UserService, PostService).
3. Configure CORS to accept connections from any host.
 
2.3.1 Configuration
 
#	Item	Value
1	server.port	8080
2	Context path	default
 
Route: /api/users/** → 8081; /api/posts/** → 8082.
 

 
2.4 Common Convention & Rules
 
Code	Description
R01	Input must be validated according to the constraints (mandatory, max length, format...) of the DB table/column
R02	All requests/responses use JSON (Content-Type: application/json)
R03	Pagination uses page (0-indexed) and size
R04	Timestamp follows ISO 8601 (YYYY-MM-DDThh:mm:ssZ)
 
2.5 Project structure requirements
1. Spring Boot packaging `jar`, JDK 21, IntelliJ, correct naming convention.
2. Use the MS SQL Server Driver for SQL Server 2016+.
3. MVC & Layer package structure:
 
Package	Content
fu.<studentId>.<servicename>.entity	Entity
fu.<studentId>.<servicename>.repository	Repository interfaces
fu.<studentId>.<servicename>.service	Service interfaces
fu.<studentId>.<servicename>.service.impl	Service impl
fu.<studentId>.<servicename>.controller	Controllers
fu.<studentId>.<servicename>.dto	DTOs
fu.<studentId>.<servicename>.config	Config
fu.<studentId>.<servicename>.common	Common
 
<servicename> = user, post, or gateway; <studentId> in lowercase.
 
4. Class & attribute names must follow the requirements.
5. Zip the project folder and submit via PEA.
6. Table and field names in the DB must exactly match the defined tables, otherwise → not graded.
7. All services and the gateway MUST have Swagger and Spring Doc API enabled.**ℹ️ If the gateway aggregates Swagger (springdoc `urls[*]` pointing to `:8081`/`:8082/v3/api-docs`), **CORS must be enabled in UserService & PostService** (@Bean `CorsFilter`) so the browser can fetch the docs cross-origin, avoiding the *"Failed to load API definition"* error.
 

 
3. Grading Policies
Violating any of the following → score of 0:
1. The requirements in 2.5 Project structure requirements and Configuration for application.properties.
 

 
4. PE Submit Guideline
Zip the source before submitting via PEA:
●Step 1: Run mvn clean to remove target/.
●Step 2: Right-click the project folder → Send to → Compressed (zipped) folder. The zip file name must start with the StudentID.
 

 
5. Dependencies & Versions (Spring Boot 4.1.0)
5.1 Common Platform
 
Component	Version	Note
Spring Boot	4.1.0	Brings Spring Framework 7, Jakarta EE 11, Hibernate 7
Spring Framework	7.0.x	Baseline JDK 17 (recommended 21); use JDK 21 per the exam spec
Spring Cloud	2025.1.2 (Oakwood)	Release train compatible with Boot 4; required for PostService (OpenFeign) & Gateway
springdoc-openapi	3.0.x	OpenAPI 3.1 + Swagger UI, supports Boot 4
mssql-jdbc	managed by Boot BOM	MS SQL Server driver
Lombok	managed by Boot BOM	Optional. The sample solution does NOT use Lombok — write explicit getters/setters/constructors to avoid cannot find symbol errors when the newer maven-compiler-plugin doesn't run Lombok's annotation processor
 
⚠️ Jakarta EE 11: all imports must be jakarta.* (no more javax.*).
 
5.2 UserService & PostService (Spring MVC)
Common dependencies for both: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, com.microsoft.sqlserver:mssql-jdbc (runtime), org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3, spring-boot-starter-test.
 
PostService only (calls UserService) additionally needs OpenFeign + Spring Cloud BOM:
 
Dependency	Note
org.springframework.cloud:spring-cloud-starter-openfeign	@FeignClient (no version specified)
spring-cloud-dependencies:2025.1.2 (BOM, import)	Manages OpenFeign version (ships 5.0.x)
 
ℹ️ UserService does not need OpenFeign. PostService must enable @EnableFeignClients and catch `FeignException.NotFound` to return the correct status 4 when the user does not exist.
 
5.3 MediaGateway (Spring Cloud Gateway – WebFlux)
 
Dependency	Note
spring-cloud-starter-gateway-server-webflux	⚠️ New name (2025.1)
org.springdoc:springdoc-openapi-starter-webflux-ui:3.0.3	Swagger UI for WebFlux
spring-cloud-dependencies:2025.1.2 (BOM, import)	Manages Spring Cloud version
spring-boot-starter-test	test
 
⚠️ The Gateway does not add spring-boot-starter-web. Route properties use the prefix spring.cloud.gateway.server.webflux.routes[*].
 

 
Status → HTTP mapping table (memorize)
1=OK (200/201) · 2=Validation (406) · 3=Duplicate username (226) · 4=NotFound (404) · 5=User not ACTIVE (400) · 0=Server (500).