using NotificationService.Data;
using NotificationService.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.Configure<MongoDbSettings>(builder.Configuration.GetSection("MongoDbSettings"));
builder.Services.Configure<EmailSettings>(builder.Configuration.GetSection("EmailSettings"));

builder.Services.AddSingleton<MongoDbContext>();
builder.Services.AddSingleton<EmailSender>();
builder.Services.AddScoped<INotificationService, NotificationServiceImpl>();

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", p => p.AllowAnyOrigin().AllowAnyHeader().AllowAnyMethod());
});

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();

// This endpoint being reachable, independent of the other 4 services, is the
// "fault isolation" property described in section 10 of the design report.
app.MapGet("/health", () => Results.Ok(new { status = "Notification Service is running" }));

app.Run();
