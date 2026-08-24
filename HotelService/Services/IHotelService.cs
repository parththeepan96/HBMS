using HotelService.Models;

namespace HotelService.Services
{
    public interface IHotelService
    {
        Task<List<HotelSummaryResponse>> SearchHotelsAsync(string? location, string? name);
        Task<List<RoomResponse>> ViewRoomsAsync(string hotelId);
        Task<AvailabilityResponse> CheckAvailabilityAsync(string hotelId, string roomId);
        Task<bool> UpdateRoomAvailabilityAsync(string hotelId, string roomId, bool isAvailable);
        Task<Hotel> CreateHotelAsync(CreateHotelRequest request);
        Task<Room?> AddRoomAsync(string hotelId, AddRoomRequest request);
        Task<decimal?> GetRoomPriceAsync(string hotelId, string roomId);
    }
}
