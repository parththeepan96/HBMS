using HotelService.Models;
using HotelService.Services;
using Microsoft.AspNetCore.Mvc;

namespace HotelService.Controllers
{
    [ApiController]
    [Route("api/hotels")]
    public class HotelController : ControllerBase
    {
        private readonly IHotelService _hotelService;

        public HotelController(IHotelService hotelService)
        {
            _hotelService = hotelService;
        }

        // GET api/hotels?location=..&name=..  -> Search Hotels() -> Hotel list (BR2)
        [HttpGet]
        public async Task<ActionResult<List<HotelSummaryResponse>>> SearchHotels(
            [FromQuery] string? location, [FromQuery] string? name)
        {
            var result = await _hotelService.SearchHotelsAsync(location, name);
            return Ok(result);
        }

        // GET api/hotels/{hotelId}/rooms -> View Rooms() -> Room List (BR2)
        [HttpGet("{hotelId}/rooms")]
        public async Task<ActionResult<List<RoomResponse>>> ViewRooms(string hotelId)
        {
            var rooms = await _hotelService.ViewRoomsAsync(hotelId);
            return Ok(rooms);
        }

        // GET api/hotels/{hotelId}/rooms/{roomId}/availability -> Check Availability() -> Yes/No
        [HttpGet("{hotelId}/rooms/{roomId}/availability")]
        public async Task<ActionResult<AvailabilityResponse>> CheckAvailability(string hotelId, string roomId)
        {
            var result = await _hotelService.CheckAvailabilityAsync(hotelId, roomId);
            return Ok(result);
        }

        // PUT api/hotels/{hotelId}/rooms/{roomId}/availability
        // Internal endpoint called by Booking Service to reserve/release a room
        [HttpPut("{hotelId}/rooms/{roomId}/availability")]
        public async Task<IActionResult> UpdateAvailability(string hotelId, string roomId,
            [FromBody] UpdateRoomAvailabilityRequest request)
        {
            var success = await _hotelService.UpdateRoomAvailabilityAsync(hotelId, roomId, request.IsAvailable);
            if (!success) return NotFound(new { message = "Room not found." });
            return Ok(new { message = "Success" });
        }

        // GET api/hotels/{hotelId}/rooms/{roomId}/price - internal helper for Booking/Payment services
        [HttpGet("{hotelId}/rooms/{roomId}/price")]
        public async Task<IActionResult> GetRoomPrice(string hotelId, string roomId)
        {
            var price = await _hotelService.GetRoomPriceAsync(hotelId, roomId);
            if (price == null) return NotFound();
            return Ok(new { pricePerNight = price });
        }

        // POST api/hotels - admin endpoint to seed hotels
        [HttpPost]
        public async Task<IActionResult> CreateHotel([FromBody] CreateHotelRequest request)
        {
            var hotel = await _hotelService.CreateHotelAsync(request);
            return CreatedAtAction(nameof(ViewRooms), new { hotelId = hotel.Id }, hotel);
        }

        // POST api/hotels/{hotelId}/rooms - admin endpoint to add a room
        [HttpPost("{hotelId}/rooms")]
        public async Task<IActionResult> AddRoom(string hotelId, [FromBody] AddRoomRequest request)
        {
            var room = await _hotelService.AddRoomAsync(hotelId, request);
            if (room == null) return NotFound(new { message = "Hotel not found." });
            return Ok(room);
        }
    }
}
