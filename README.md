# HBMS — Hotel Booking Management System

Group 2

```
HBMS/
├── ApiGateway/          C# (.NET 8) + Ocelot   — single entry point, routing
├── UserService/         C# (.NET 8) + MongoDB  — register, login, profile
├── HotelService/        C# (.NET 8) + MongoDB  — search hotels, rooms, availability
├── BookingService/      Java (Spring Boot) + MongoDB — create/cancel booking, history
├── PaymentService/      Java (Spring Boot) + MongoDB — process payment, refund
├── NotificationService/ C# (.NET 8) + MongoDB  — confirmation & cancellation emails
└── Frontend/            HTML/CSS/JS            — talks only to the API Gateway
```


To Locally run

1. To start front end

cd Frontend
npx serve

2. To start ApiGateway

cd ApiGateway
dotnet run --urls "http://localhost:5000" 

3. To start UserService

cd UserService
dotnet run --urls "http://localhost:5001" 

4. To start HotelService

cd HotelService
dotnet run --urls "http://localhost:5002" 

5. To Start BookingService

mvn spring-boot:run  

6. To start PaymentService

mvn spring-boot:run  

7. To start NotificationService

cd NotificationService
dotnet run --urls "http://localhost:5005" 