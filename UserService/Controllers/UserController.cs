using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using UserService.Models;
using UserService.Services;

namespace UserService.Controllers
{
    [ApiController]
    [Route("api/users")]
    public class UserController : ControllerBase
    {
        private readonly IUserService _userService;
        private readonly ILogger<UserController> _logger;

        public UserController(IUserService userService, ILogger<UserController> logger)
        {
            _userService = userService;
            _logger = logger;
        }

        // POST api/users/register  -> Register() -> Output: User ID   (BR1)
        [HttpPost("register")]
        public async Task<ActionResult<RegisterResponse>> Register([FromBody] RegisterRequest request)
        {
            try
            {
                var result = await _userService.RegisterAsync(request);
                return CreatedAtAction(nameof(GetProfile), new { userId = result.UserId }, result);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
        }

        // POST api/users/login  -> Login() -> Output: JWT auth token  (BR1)
        [HttpPost("login")]
        public async Task<ActionResult<LoginResponse>> Login([FromBody] LoginRequest request)
        {
            var result = await _userService.LoginAsync(request);
            if (result == null)
                return Unauthorized(new { message = "Invalid email or password." });

            return Ok(result);
        }

        // PUT api/users/{userId}/profile -> Update Profile() -> Output: Success (BR1)
        [HttpPut("{userId}/profile")]
        [Authorize]
        public async Task<IActionResult> UpdateProfile(string userId, [FromBody] UpdateProfileRequest request)
        {
            var success = await _userService.UpdateProfileAsync(userId, request);
            if (!success) return NotFound(new { message = "User not found." });
            return Ok(new { message = "Success" });
        }

        // GET api/users/{userId}
        [HttpGet("{userId}")]
        public async Task<ActionResult<UserProfileResponse>> GetProfile(string userId)
        {
            var profile = await _userService.GetProfileAsync(userId);
            if (profile == null) return NotFound();
            return Ok(profile);
        }

        // GET api/users/{userId}/validate
        // Internal endpoint used by Booking Service (Figure 4, step 7: "Validate user")
        [HttpGet("{userId}/validate")]
        public async Task<ActionResult<ValidateUserResponse>> Validate(string userId)
        {
            var result = await _userService.ValidateUserAsync(userId);
            return Ok(result);
        }
    }
}
