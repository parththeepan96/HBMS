using UserService.Models;

namespace UserService.Services
{
    public interface IUserService
    {
        Task<RegisterResponse> RegisterAsync(RegisterRequest request);
        Task<LoginResponse?> LoginAsync(LoginRequest request);
        Task<bool> UpdateProfileAsync(string userId, UpdateProfileRequest request);
        Task<UserProfileResponse?> GetProfileAsync(string userId);
        Task<ValidateUserResponse> ValidateUserAsync(string userId);
    }
}
