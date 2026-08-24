using Ocelot.DependencyInjection;
using Ocelot.Middleware;

var builder = WebApplication.CreateBuilder(args);

// ocelot.json is a template with {{TOKEN}} placeholders for each downstream service's
// scheme/host/port, so the same file works unmodified for local dev (defaults below match
// each service's local port) and for a real deployment (set the env vars and each service
// gets routed to its actual public URL instead of localhost).
string Env(string name, string fallback) =>
    Environment.GetEnvironmentVariable(name) is string v && !string.IsNullOrWhiteSpace(v) ? v : fallback;

var ocelotTemplate = File.ReadAllText(Path.Combine(builder.Environment.ContentRootPath, "ocelot.json"));
var ocelotJson = ocelotTemplate
    .Replace("{{USER_SCHEME}}", Env("USER_SERVICE_SCHEME", "http"))
    .Replace("{{USER_HOST}}", Env("USER_SERVICE_HOST", "localhost"))
    .Replace("{{USER_PORT}}", Env("USER_SERVICE_PORT", "5001"))
    .Replace("{{HOTEL_SCHEME}}", Env("HOTEL_SERVICE_SCHEME", "http"))
    .Replace("{{HOTEL_HOST}}", Env("HOTEL_SERVICE_HOST", "localhost"))
    .Replace("{{HOTEL_PORT}}", Env("HOTEL_SERVICE_PORT", "5002"))
    .Replace("{{BOOKING_SCHEME}}", Env("BOOKING_SERVICE_SCHEME", "http"))
    .Replace("{{BOOKING_HOST}}", Env("BOOKING_SERVICE_HOST", "localhost"))
    .Replace("{{BOOKING_PORT}}", Env("BOOKING_SERVICE_PORT", "5003"))
    .Replace("{{PAYMENT_SCHEME}}", Env("PAYMENT_SERVICE_SCHEME", "http"))
    .Replace("{{PAYMENT_HOST}}", Env("PAYMENT_SERVICE_HOST", "localhost"))
    .Replace("{{PAYMENT_PORT}}", Env("PAYMENT_SERVICE_PORT", "5004"))
    .Replace("{{NOTIFICATION_SCHEME}}", Env("NOTIFICATION_SERVICE_SCHEME", "http"))
    .Replace("{{NOTIFICATION_HOST}}", Env("NOTIFICATION_SERVICE_HOST", "localhost"))
    .Replace("{{NOTIFICATION_PORT}}", Env("NOTIFICATION_SERVICE_PORT", "5005"))
    .Replace("{{GATEWAY_BASE_URL}}", Env("GATEWAY_BASE_URL", "http://localhost:5000"));

var generatedOcelotPath = Path.Combine(builder.Environment.ContentRootPath, "ocelot.generated.json");
File.WriteAllText(generatedOcelotPath, ocelotJson);
builder.Configuration.AddJsonFile(generatedOcelotPath, optional: false, reloadOnChange: false);

builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", p => p.AllowAnyOrigin().AllowAnyHeader().AllowAnyMethod());
});

// Ocelot provides routing, and can be extended with authentication (JWT) and
// load-balancing per downstream route, exactly as described in report section 4.
builder.Services.AddOcelot(builder.Configuration);

var app = builder.Build();

app.UseCors("AllowAll");

app.MapGet("/health", () => Results.Ok(new { status = "API Gateway is running" }));

await app.UseOcelot();

app.Run();
