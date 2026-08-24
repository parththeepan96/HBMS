using HotelService.Data;
using HotelService.Models;
using MongoDB.Driver;

namespace HotelService.Services
{
    public class HotelServiceImpl : IHotelService
    {
        private readonly MongoDbContext _context;

        public HotelServiceImpl(MongoDbContext context)
        {
            _context = context;
        }

        // Search Hotels() - Input: Location/name -> Output: Hotel list
        public async Task<List<HotelSummaryResponse>> SearchHotelsAsync(string? location, string? name)
        {
            var filterBuilder = Builders<Hotel>.Filter;
            var filter = filterBuilder.Empty;

            if (!string.IsNullOrWhiteSpace(location))
                filter &= filterBuilder.Regex(h => h.Location, new MongoDB.Bson.BsonRegularExpression(location, "i"));

            if (!string.IsNullOrWhiteSpace(name))
                filter &= filterBuilder.Regex(h => h.Name, new MongoDB.Bson.BsonRegularExpression(name, "i"));

            var hotels = await _context.Hotels.Find(filter).ToListAsync();

            return hotels.Select(h => new HotelSummaryResponse
            {
                HotelId = h.Id,
                Name = h.Name,
                Location = h.Location,
                Description = h.Description
            }).ToList();
        }

        // View Rooms() - Input: Hotel name (ID) -> Output: Room List
        public async Task<List<RoomResponse>> ViewRoomsAsync(string hotelId)
        {
            var hotel = await _context.Hotels.Find(h => h.Id == hotelId).FirstOrDefaultAsync();
            if (hotel == null) return new List<RoomResponse>();

            return hotel.Rooms.Select(r => new RoomResponse
            {
                RoomId = r.RoomId,
                Type = r.Type,
                PricePerNight = r.PricePerNight,
                Facilities = r.Facilities,
                IsAvailable = r.IsAvailable
            }).ToList();
        }

        // Check Availability() - Input: Room ID -> Output: Yes/No
        public async Task<AvailabilityResponse> CheckAvailabilityAsync(string hotelId, string roomId)
        {
            var hotel = await _context.Hotels.Find(h => h.Id == hotelId).FirstOrDefaultAsync();
            var room = hotel?.Rooms.FirstOrDefault(r => r.RoomId == roomId);
            return new AvailabilityResponse { Available = room?.IsAvailable ?? false };
        }

        // Used by Booking Service when reserving / releasing a room (Figure 4, steps 9-13)
        public async Task<bool> UpdateRoomAvailabilityAsync(string hotelId, string roomId, bool isAvailable)
        {
            var filter = Builders<Hotel>.Filter.And(
                Builders<Hotel>.Filter.Eq(h => h.Id, hotelId),
                Builders<Hotel>.Filter.ElemMatch(h => h.Rooms, r => r.RoomId == roomId)
            );

            var update = Builders<Hotel>.Update.Set("rooms.$.isAvailable", isAvailable);
            var result = await _context.Hotels.UpdateOneAsync(filter, update);
            return result.ModifiedCount > 0;
        }

        public async Task<Hotel> CreateHotelAsync(CreateHotelRequest request)
        {
            var hotel = new Hotel
            {
                Name = request.Name,
                Location = request.Location,
                Description = request.Description
            };
            await _context.Hotels.InsertOneAsync(hotel);
            return hotel;
        }

        public async Task<Room?> AddRoomAsync(string hotelId, AddRoomRequest request)
        {
            var hotel = await _context.Hotels.Find(h => h.Id == hotelId).FirstOrDefaultAsync();
            if (hotel == null) return null;

            var room = new Room
            {
                Type = request.Type,
                PricePerNight = request.PricePerNight,
                Facilities = request.Facilities,
                IsAvailable = true
            };

            var update = Builders<Hotel>.Update.Push(h => h.Rooms, room);
            await _context.Hotels.UpdateOneAsync(h => h.Id == hotelId, update);
            return room;
        }

        public async Task<decimal?> GetRoomPriceAsync(string hotelId, string roomId)
        {
            var hotel = await _context.Hotels.Find(h => h.Id == hotelId).FirstOrDefaultAsync();
            return hotel?.Rooms.FirstOrDefault(r => r.RoomId == roomId)?.PricePerNight;
        }
    }
}
