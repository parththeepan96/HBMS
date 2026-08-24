namespace NotificationService.Models
{
    // Input: Booking ID -> Send Confirmation() -> Output: Email Sent
    public class SendConfirmationRequest
    {
        public string BookingId { get; set; } = string.Empty;
        public string RecipientEmail { get; set; } = string.Empty;
        public string CustomerName { get; set; } = string.Empty;
        public string HotelName { get; set; } = string.Empty;
        public string RoomType { get; set; } = string.Empty;
        public decimal AmountPaid { get; set; }
    }

    // Input: Booking ID -> Send Cancellation() -> Output: Notification Sent
    public class SendCancellationRequest
    {
        public string BookingId { get; set; } = string.Empty;
        public string RecipientEmail { get; set; } = string.Empty;
        public string CustomerName { get; set; } = string.Empty;
        public decimal RefundAmount { get; set; }
    }

    public class NotificationResponse
    {
        public string Status { get; set; } = string.Empty; // "Email Sent" | "Notification Sent"
        public string NotificationId { get; set; } = string.Empty;
    }
}
