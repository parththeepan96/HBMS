using Microsoft.AspNetCore.Mvc;
using NotificationService.Models;
using NotificationService.Services;

namespace NotificationService.Controllers
{
    [ApiController]
    [Route("api/notifications")]
    public class NotificationController : ControllerBase
    {
        private readonly INotificationService _notificationService;

        public NotificationController(INotificationService notificationService)
        {
            _notificationService = notificationService;
        }

        // POST api/notifications/confirmation -> Send Confirmation() -> Email Sent (BR5)
        [HttpPost("confirmation")]
        public async Task<ActionResult<NotificationResponse>> SendConfirmation([FromBody] SendConfirmationRequest request)
        {
            var result = await _notificationService.SendConfirmationAsync(request);
            return Ok(result);
        }

        // POST api/notifications/cancellation -> Send Cancellation() -> Notification Sent (BR5)
        [HttpPost("cancellation")]
        public async Task<ActionResult<NotificationResponse>> SendCancellation([FromBody] SendCancellationRequest request)
        {
            var result = await _notificationService.SendCancellationAsync(request);
            return Ok(result);
        }

        // GET api/notifications/booking/{bookingId}
        [HttpGet("booking/{bookingId}")]
        public async Task<IActionResult> GetByBooking(string bookingId)
        {
            var results = await _notificationService.GetByBookingIdAsync(bookingId);
            return Ok(results);
        }
    }
}
