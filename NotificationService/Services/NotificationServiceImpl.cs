using MongoDB.Driver;
using NotificationService.Data;
using NotificationService.Models;

namespace NotificationService.Services
{
    public class NotificationServiceImpl : INotificationService
    {
        private readonly MongoDbContext _context;
        private readonly EmailSender _emailSender;

        public NotificationServiceImpl(MongoDbContext context, EmailSender emailSender)
        {
            _context = context;
            _emailSender = emailSender;
        }

        // Send Confirmation() - Input: Booking ID -> Output: Email Sent
        public async Task<NotificationResponse> SendConfirmationAsync(SendConfirmationRequest request)
        {
            var subject = $"Booking Confirmed - #{request.BookingId}";
            var body = $"Hi {request.CustomerName},\n\n" +
                       $"Your booking at {request.HotelName} ({request.RoomType}) is confirmed.\n" +
                       $"Amount paid: {request.AmountPaid:C}\n\n" +
                       $"Thank you for choosing HBMS.";

            await _emailSender.SendAsync(request.RecipientEmail, subject, body);

            var notification = new Notification
            {
                BookingId = request.BookingId,
                RecipientEmail = request.RecipientEmail,
                Type = NotificationType.BookingConfirmation,
                Message = body,
                Status = "Email Sent"
            };
            await _context.Notifications.InsertOneAsync(notification);

            return new NotificationResponse { Status = "Email Sent", NotificationId = notification.Id };
        }

        // Send Cancellation() - Input: Booking ID -> Output: Notification Sent
        public async Task<NotificationResponse> SendCancellationAsync(SendCancellationRequest request)
        {
            var subject = $"Booking Cancelled - #{request.BookingId}";
            var body = $"Hi {request.CustomerName},\n\n" +
                       $"Your booking #{request.BookingId} has been cancelled.\n" +
                       $"Refund amount: {request.RefundAmount:C}\n\n" +
                       $"We hope to see you again soon.";

            await _emailSender.SendAsync(request.RecipientEmail, subject, body);

            var notification = new Notification
            {
                BookingId = request.BookingId,
                RecipientEmail = request.RecipientEmail,
                Type = NotificationType.BookingCancellation,
                Message = body,
                Status = "Notification Sent"
            };
            await _context.Notifications.InsertOneAsync(notification);

            return new NotificationResponse { Status = "Notification Sent", NotificationId = notification.Id };
        }

        public async Task<List<Notification>> GetByBookingIdAsync(string bookingId)
        {
            return await _context.Notifications.Find(n => n.BookingId == bookingId).ToListAsync();
        }
    }
}
