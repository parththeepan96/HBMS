namespace HotelService.Models
{
    public class HotelSummaryResponse
    {
        public string HotelId { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Location { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
    }

    public class RoomResponse
    {
        public string RoomId { get; set; } = string.Empty;
        public string Type { get; set; } = string.Empty;
        public decimal PricePerNight { get; set; }
        public List<string> Facilities { get; set; } = new();
        public bool IsAvailable { get; set; }
    }

    public class AvailabilityResponse
    {
        public bool Available { get; set; } // Yes/No per Table 1
    }

    // Called by Booking Service when a booking is confirmed / cancelled
    public class UpdateRoomAvailabilityRequest
    {
        public bool IsAvailable { get; set; }
    }

    public class CreateHotelRequest
    {
        public string Name { get; set; } = string.Empty;
        public string Location { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
    }

    public class AddRoomRequest
    {
        public string Type { get; set; } = string.Empty;
        public decimal PricePerNight { get; set; }
        public List<string> Facilities { get; set; } = new();
    }
}
