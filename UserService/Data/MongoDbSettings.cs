namespace UserService.Data
{
    // Strongly-typed binding of the "MongoDbSettings" section in appsettings.json
    public class MongoDbSettings
    {
        public string ConnectionString { get; set; } = string.Empty;
        public string DatabaseName { get; set; } = string.Empty;
        public string UsersCollectionName { get; set; } = string.Empty;
    }
}
