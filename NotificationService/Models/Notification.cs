using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace NotificationService.Models
{
    public enum NotificationType
    {
        BookingConfirmation,
        BookingCancellation
    }

    // Maps to Table 1 - Notification Service: Send Confirmation(), Send Cancellation()
    public class Notification
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = ObjectId.GenerateNewId().ToString();

        [BsonElement("bookingId")]
        public string BookingId { get; set; } = string.Empty;

        [BsonElement("recipientEmail")]
        public string RecipientEmail { get; set; } = string.Empty;

        [BsonElement("type")]
        [BsonRepresentation(BsonType.String)]
        public NotificationType Type { get; set; }

        [BsonElement("message")]
        public string Message { get; set; } = string.Empty;

        [BsonElement("status")]
        public string Status { get; set; } = "Sent"; // "Email Sent" / "Notification Sent"

        [BsonElement("sentAt")]
        public DateTime SentAt { get; set; } = DateTime.UtcNow;
    }
}
