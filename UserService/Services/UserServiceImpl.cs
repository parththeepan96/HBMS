using MongoDB.Driver;
using UserService.Data;
using UserService.Models;

namespace UserService.Services
{
    public class UserServiceImpl : IUserService
    {
        private readonly MongoDbContext _context;
        private readonly JwtTokenService _jwtTokenService;

        public UserServiceImpl(MongoDbContext context, JwtTokenService jwtTokenService)
        {
            _context = context;
            _jwtTokenService = jwtTokenService;
        }

        public async Task<RegisterResponse> RegisterAsync(RegisterRequest request)
        {
            var existing = await _context.Users.Find(u => u.Email == request.Email).FirstOrDefaultAsync();
            if (existing != null)
                throw new InvalidOperationException("A user with this email already exists.");

            var user = new User
            {
                Name = request.Name,
                Email = request.Email,
                Phone = request.Phone,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password)
            };

            await _context.Users.InsertOneAsync(user);

            return new RegisterResponse { UserId = user.Id };
        }

        public async Task<LoginResponse?> LoginAsync(LoginRequest request)
        {
            var user = await _context.Users.Find(u => u.Email == request.Email).FirstOrDefaultAsync();
            if (user == null || !BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash))
                return null;

            var token = _jwtTokenService.GenerateToken(user);

            return new LoginResponse
            {
                Token = token,
                UserId = user.Id,
                Name = user.Name
            };
        }

        public async Task<bool> UpdateProfileAsync(string userId, UpdateProfileRequest request)
        {
            var update = Builders<User>.Update
                .Set(u => u.Name, request.Name)
                .Set(u => u.Phone, request.Phone);

            // Use MatchedCount, not ModifiedCount: if the submitted name/phone are identical to
            // what's already stored, Mongo reports zero modifications even though the user was
            // found and the update was valid - that shouldn't surface as "User not found."
            var result = await _context.Users.UpdateOneAsync(u => u.Id == userId, update);
            return result.MatchedCount > 0;
        }

        public async Task<UserProfileResponse?> GetProfileAsync(string userId)
        {
            var user = await _context.Users.Find(u => u.Id == userId).FirstOrDefaultAsync();
            if (user == null) return null;

            return new UserProfileResponse
            {
                UserId = user.Id,
                Name = user.Name,
                Email = user.Email,
                Phone = user.Phone
            };
        }

        public async Task<ValidateUserResponse> ValidateUserAsync(string userId)
        {
            var user = await _context.Users.Find(u => u.Id == userId).FirstOrDefaultAsync();
            if (user == null)
                return new ValidateUserResponse { IsValid = false };

            return new ValidateUserResponse
            {
                IsValid = true,
                UserId = user.Id,
                Name = user.Name,
                Email = user.Email
            };
        }
    }
}
