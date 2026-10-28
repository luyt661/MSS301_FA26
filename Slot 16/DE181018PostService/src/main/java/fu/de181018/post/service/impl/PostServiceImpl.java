package fu.de181018.post.service.impl;

import feign.FeignException;
import fu.de181018.post.client.UserClient;
import fu.de181018.post.common.PostStatus;
import fu.de181018.post.common.ResourceNotFoundException;
import fu.de181018.post.common.UserNotActiveException;
import fu.de181018.post.common.ValidationException;
import fu.de181018.post.dto.PageDTO;
import fu.de181018.post.dto.PostDTO;
import fu.de181018.post.dto.PostDetailDTO;
import fu.de181018.post.dto.UserApiResponse;
import fu.de181018.post.dto.UserDTO;
import fu.de181018.post.entity.Post;
import fu.de181018.post.repository.PostRepository;
import fu.de181018.post.service.PostService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final PostRepository postRepository;
    private final UserClient userClient;

    public PostServiceImpl(PostRepository postRepository, UserClient userClient) {
        this.postRepository = postRepository;
        this.userClient = userClient;
    }

    @Override
    public PostDTO createPost(PostDTO dto) {
        if (dto == null) {
            throw new ValidationException("Validation failed");
        }

        // Validate basic fields
        if (dto.getAdvertiserName() == null || dto.getAdvertiserName().trim().isEmpty() || dto.getAdvertiserName().trim().length() > 100) {
            throw new ValidationException("Validation failed");
        }

        if (dto.getAdvertiserEmail() == null || dto.getAdvertiserEmail().trim().isEmpty()
                || dto.getAdvertiserEmail().trim().length() > 100 || !EMAIL_PATTERN.matcher(dto.getAdvertiserEmail().trim()).matches()) {
            throw new ValidationException("Validation failed");
        }

        if (dto.getAdvertiserPhone() == null || dto.getAdvertiserPhone().trim().isEmpty() || dto.getAdvertiserPhone().trim().length() > 20) {
            throw new ValidationException("Validation failed");
        }

        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new ValidationException("Validation failed");
        }

        if (!dto.getEndDate().after(dto.getStartDate())) {
            throw new ValidationException("Validation failed (endDate <= startDate)");
        }

        if (dto.getNumberOfPosts() == null || dto.getNumberOfPosts() < 1) {
            throw new ValidationException("Validation failed (numberOfPosts must be >= 1)");
        }

        if (dto.getUserId() == null) {
            throw new ValidationException("Validation failed (userId is required)");
        }

        // Fetch User from UserService via Feign
        UserDTO user = fetchUser(dto.getUserId());

        // Check if user is ACTIVE
        if (user.getStatus() == null || !"ACTIVE".equalsIgnoreCase(user.getStatus().trim())) {
            throw new UserNotActiveException("User is not ACTIVE");
        }

        // Check numberOfPosts <= maxDailyPosts
        if (user.getMaxDailyPosts() != null && dto.getNumberOfPosts() > user.getMaxDailyPosts()) {
            throw new ValidationException("Validation failed (numberOfPosts exceeds creator's maxDailyPosts: " + user.getMaxDailyPosts() + ")");
        }

        // Calculate totalCost = ratePerPost * number of days
        long days = calculateDays(dto.getStartDate(), dto.getEndDate());
        BigDecimal rate = (user.getRatePerPost() != null) ? user.getRatePerPost() : BigDecimal.ZERO;
        BigDecimal totalCost = rate.multiply(BigDecimal.valueOf(days));

        // Default status = SCHEDULED
        PostStatus status = PostStatus.SCHEDULED;
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            if (!PostStatus.isValid(dto.getStatus())) {
                throw new ValidationException("Validation failed (invalid status)");
            }
            status = PostStatus.fromString(dto.getStatus());
        }

        Post post = new Post();
        post.setAdvertiserName(dto.getAdvertiserName().trim());
        post.setAdvertiserEmail(dto.getAdvertiserEmail().trim());
        post.setAdvertiserPhone(dto.getAdvertiserPhone().trim());
        post.setStartDate(dto.getStartDate());
        post.setEndDate(dto.getEndDate());
        post.setNumberOfPosts(dto.getNumberOfPosts());
        post.setTotalCost(totalCost);
        post.setStatus(status);
        post.setUserId(dto.getUserId());

        Post savedPost = postRepository.save(post);
        return toDTO(savedPost);
    }

    @Override
    public PostDTO updatePost(Long postId, PostDTO dto) {
        if (postId == null || dto == null) {
            throw new ValidationException("Validation failed");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("postId not found"));

        Date newStart = (dto.getStartDate() != null) ? dto.getStartDate() : post.getStartDate();
        Date newEnd = (dto.getEndDate() != null) ? dto.getEndDate() : post.getEndDate();

        if (!newEnd.after(newStart)) {
            throw new ValidationException("Validation failed (endDate <= startDate)");
        }
        post.setStartDate(newStart);
        post.setEndDate(newEnd);

        if (dto.getAdvertiserName() != null) {
            String name = dto.getAdvertiserName().trim();
            if (name.isEmpty() || name.length() > 100) {
                throw new ValidationException("Validation failed");
            }
            post.setAdvertiserName(name);
        }

        if (dto.getAdvertiserEmail() != null) {
            String email = dto.getAdvertiserEmail().trim();
            if (email.isEmpty() || email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
                throw new ValidationException("Validation failed");
            }
            post.setAdvertiserEmail(email);
        }

        if (dto.getAdvertiserPhone() != null) {
            String phone = dto.getAdvertiserPhone().trim();
            if (phone.isEmpty() || phone.length() > 20) {
                throw new ValidationException("Validation failed");
            }
            post.setAdvertiserPhone(phone);
        }

        if (dto.getUserId() != null && !dto.getUserId().equals(post.getUserId())) {
            UserDTO newUser = fetchUser(dto.getUserId());
            if (!"ACTIVE".equalsIgnoreCase(newUser.getStatus())) {
                throw new UserNotActiveException("User is not ACTIVE");
            }
            post.setUserId(dto.getUserId());
        }

        // Fetch creator to recalculate rate and validate maxDailyPosts
        UserDTO user = fetchUser(post.getUserId());

        if (dto.getNumberOfPosts() != null) {
            if (dto.getNumberOfPosts() < 1 || (user.getMaxDailyPosts() != null && dto.getNumberOfPosts() > user.getMaxDailyPosts())) {
                throw new ValidationException("Validation failed (numberOfPosts invalid)");
            }
            post.setNumberOfPosts(dto.getNumberOfPosts());
        }

        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            if (!PostStatus.isValid(dto.getStatus())) {
                throw new ValidationException("Validation failed (invalid status)");
            }
            post.setStatus(PostStatus.fromString(dto.getStatus()));
        }

        // Recalculate totalCost based on creator's current rate
        long days = calculateDays(post.getStartDate(), post.getEndDate());
        BigDecimal rate = (user.getRatePerPost() != null) ? user.getRatePerPost() : BigDecimal.ZERO;
        post.setTotalCost(rate.multiply(BigDecimal.valueOf(days)));

        Post updatedPost = postRepository.save(post);
        return toDTO(updatedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailDTO getPostDetail(Long postId) {
        if (postId == null) {
            throw new ValidationException("Validation failed");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found"));

        UserDTO user = fetchUserSilently(post.getUserId());
        return toDetailDTO(post, user);
    }

    @Override
    public void cancelPost(Long postId) {
        if (postId == null) {
            throw new ValidationException("Validation failed");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found"));

        post.setStatus(PostStatus.CANCELLED);
        postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO getPostList(Integer page, Integer size, String advertiserName, String statusStr) {
        int pageIndex = (page == null) ? 0 : page;
        int pageSize = (size == null) ? 10 : size;

        if (pageSize > 100 || pageSize < 1 || pageIndex < 0) {
            throw new ValidationException("Validation failed (size > 100)");
        }

        if (statusStr != null && !statusStr.trim().isEmpty() && !PostStatus.isValid(statusStr)) {
            throw new ValidationException("Validation failed");
        }

        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (advertiserName != null && !advertiserName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("advertiserName")), "%" + advertiserName.trim().toLowerCase() + "%"));
            }
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), PostStatus.fromString(statusStr)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.ASC, "postId"));
        Page<Post> postPage = postRepository.findAll(spec, pageable);

        // Cache users by id to avoid redundant Feign calls during pagination
        Map<Long, UserDTO> userCache = new HashMap<>();
        List<PostDetailDTO> content = postPage.getContent().stream()
                .map(post -> {
                    UserDTO user = userCache.computeIfAbsent(post.getUserId(), this::fetchUserSilently);
                    return toDetailDTO(post, user);
                })
                .collect(Collectors.toList());

        return new PageDTO(
                postPage.getSize(),
                postPage.getNumber(),
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                postPage.isFirst(),
                postPage.isLast(),
                content
        );
    }

    private UserDTO fetchUser(Long userId) {
        try {
            UserApiResponse response = userClient.getUserById(userId);
            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("userId not found");
            }
            return response.getData();
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("userId not found");
        }
    }

    private UserDTO fetchUserSilently(Long userId) {
        try {
            UserApiResponse response = userClient.getUserById(userId);
            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception ignored) {
        }
        UserDTO fallback = new UserDTO();
        fallback.setUserId(userId);
        return fallback;
    }

    private long calculateDays(Date startDate, Date endDate) {
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long days = ChronoUnit.DAYS.between(start, end);
        return (days <= 0) ? 1 : days;
    }

    private PostDTO toDTO(Post post) {
        return new PostDTO(
                post.getPostId(),
                post.getAdvertiserName(),
                post.getAdvertiserEmail(),
                post.getAdvertiserPhone(),
                post.getStartDate(),
                post.getEndDate(),
                post.getNumberOfPosts(),
                post.getTotalCost(),
                post.getStatus() != null ? post.getStatus().name() : null,
                post.getUserId()
        );
    }

    private PostDetailDTO toDetailDTO(Post post, UserDTO user) {
        return new PostDetailDTO(
                post.getPostId(),
                post.getAdvertiserName(),
                post.getAdvertiserEmail(),
                post.getAdvertiserPhone(),
                post.getStartDate(),
                post.getEndDate(),
                post.getNumberOfPosts(),
                post.getTotalCost(),
                post.getStatus() != null ? post.getStatus().name() : null,
                user
        );
    }
}
