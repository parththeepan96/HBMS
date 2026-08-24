using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace HotelService.Models
{
    // Maps to Table 1 - Hotel Service: Search Hotels(), View Rooms(), Check Availability()
    public class Hotel
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = ObjectId.GenerateNewId().ToString();

        [BsonElement("name")]
        public string Name { get; set; } = string.Empty;

        [BsonElement("location")]
        public string Location { get; set; } = string.Empty;

        [BsonElement("description")]
        public string Description { get; set; } = string.Empty;

        [BsonElement("rooms")]
        public List<Room> Rooms { get; set; } = new();
    }

    public class Room
    {
        [BsonElement("roomId")]
        public string RoomId { get; set; } = ObjectId.GenerateNewId().ToString();

        [BsonElement("type")]
        public string Type { get; set; } = string.Empty; // e.g. Single, Double, Suite

        [BsonElement("pricePerNight")]
        public decimal PricePerNight { get; set; }

        [BsonElement("facilities")]
        public List<string> Facilities { get; set; } = new();

        [BsonElement("isAvailable")]
        public bool IsAvailable { get; set; } = true;
    }
}
