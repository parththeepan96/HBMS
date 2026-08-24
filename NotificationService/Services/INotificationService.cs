using NotificationService.Models;

namespace NotificationService.Services
{
    public interface INotificationService
    {
        Task<NotificationResponse> SendConfirmationAsync(SendConfirmationRequest request);
        Task<NotificationResponse> SendCancellationAsync(SendCancellationRequest request);
        Task<List<Notification>> GetByBookingIdAsync(string bookingId);
    }
}
