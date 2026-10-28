package fu.de181018.user.service.impl;

import fu.de181018.user.common.DuplicateUsernameException;
import fu.de181018.user.common.Role;
import fu.de181018.user.common.UserNotFoundException;
import fu.de181018.user.common.UserStatus;
import fu.de181018.user.common.ValidationException;
import fu.de181018.user.dto.PageDTO;
import fu.de181018.user.dto.UserDTO;
import fu.de181018.user.entity.User;
import fu.de181018.user.repository.UserRepository;
import fu.de181018.user.service.UserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO createUser(UserDTO dto) {
        if (dto == null) {
            throw new ValidationException("Validation failed");
        }

        // Validate username
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty() || dto.getUsername().trim().length() > 30) {
            throw new ValidationException("Validation failed");
        }

        // Validate email
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty() || dto.getEmail().trim().length() > 100 || !EMAIL_PATTERN.matcher(dto.getEmail().trim()).matches()) {
            throw new ValidationException("Validation failed");
        }

        // Validate role
        if (dto.getRole() == null || !Role.isValid(dto.getRole())) {
            throw new ValidationException("Validation failed");
        }

        // Validate ratePerPost
        if (dto.getRatePerPost() == null || dto.getRatePerPost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Validation failed");
        }

        // Validate maxDailyPosts (1..10)
        if (dto.getMaxDailyPosts() == null || dto.getMaxDailyPosts() < 1 || dto.getMaxDailyPosts() > 10) {
            throw new ValidationException("Validation failed");
        }

        // Validate level (>= 1)
        if (dto.getLevel() == null || dto.getLevel() < 1) {
            throw new ValidationException("Validation failed");
        }

        // Validate status: Default = ACTIVE if not provided
        UserStatus status = UserStatus.ACTIVE;
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            if (!UserStatus.isValid(dto.getStatus())) {
                throw new ValidationException("Validation failed");
            }
            status = UserStatus.fromString(dto.getStatus());
        }

        // Check duplicate username
        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new DuplicateUsernameException("Duplicate username");
        }

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setEmail(dto.getEmail().trim());
        user.setRole(Role.fromString(dto.getRole()));
        user.setRatePerPost(dto.getRatePerPost());
        user.setMaxDailyPosts(dto.getMaxDailyPosts());
        user.setLevel(dto.getLevel());
        user.setStatus(status);

        User savedUser = userRepository.save(user);
        return toDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO dto) {
        if (userId == null || dto == null) {
            throw new ValidationException("Validation failed");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Partial update: validate only fields that are provided
        if (dto.getUsername() != null) {
            String uname = dto.getUsername().trim();
            if (uname.isEmpty() || uname.length() > 30) {
                throw new ValidationException("Validation failed");
            }
            if (userRepository.existsByUsernameAndUserIdNot(uname, userId)) {
                throw new DuplicateUsernameException("Duplicate username");
            }
            user.setUsername(uname);
        }

        if (dto.getEmail() != null) {
            String email = dto.getEmail().trim();
            if (email.isEmpty() || email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
                throw new ValidationException("Validation failed");
            }
            user.setEmail(email);
        }

        if (dto.getRole() != null) {
            if (!Role.isValid(dto.getRole())) {
                throw new ValidationException("Validation failed");
            }
            user.setRole(Role.fromString(dto.getRole()));
        }

        if (dto.getRatePerPost() != null) {
            if (dto.getRatePerPost().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Validation failed");
            }
            user.setRatePerPost(dto.getRatePerPost());
        }

        if (dto.getMaxDailyPosts() != null) {
            if (dto.getMaxDailyPosts() < 1 || dto.getMaxDailyPosts() > 10) {
                throw new ValidationException("Validation failed");
            }
            user.setMaxDailyPosts(dto.getMaxDailyPosts());
        }

        if (dto.getLevel() != null) {
            if (dto.getLevel() < 1) {
                throw new ValidationException("Validation failed");
            }
            user.setLevel(dto.getLevel());
        }

        if (dto.getStatus() != null) {
            if (!UserStatus.isValid(dto.getStatus())) {
                throw new ValidationException("Validation failed");
            }
            user.setStatus(UserStatus.fromString(dto.getStatus()));
        }

        User updatedUser = userRepository.save(user);
        return toDTO(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        if (userId == null) {
            throw new ValidationException("Validation failed");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toDTO(user);
    }

    @Override
    public void deactivateUser(Long userId) {
        if (userId == null) {
            throw new ValidationException("Validation failed");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO getUserList(Integer page, Integer size, String roleStr, String statusStr) {
        int pageIndex = (page == null) ? 0 : page;
        int pageSize = (size == null) ? 10 : size;

        // Validation rule: size > 100 or size < 1 or page < 0
        if (pageSize > 100 || pageSize < 1 || pageIndex < 0) {
            throw new ValidationException("Validation failed (size > 100)");
        }

        if (roleStr != null && !roleStr.trim().isEmpty() && !Role.isValid(roleStr)) {
            throw new ValidationException("Validation failed");
        }

        if (statusStr != null && !statusStr.trim().isEmpty() && !UserStatus.isValid(statusStr)) {
            throw new ValidationException("Validation failed");
        }

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (roleStr != null && !roleStr.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("role"), Role.fromString(roleStr)));
            }
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), UserStatus.fromString(statusStr)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.ASC, "userId"));
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserDTO> content = userPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageDTO(
                userPage.getSize(),
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.isFirst(),
                userPage.isLast(),
                content
        );
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getRatePerPost(),
                user.getMaxDailyPosts(),
                user.getLevel(),
                user.getStatus() != null ? user.getStatus().name() : null
        );
    }
}
