package fu.de181018.user.service;

import fu.de181018.user.dto.PageDTO;
import fu.de181018.user.dto.UserDTO;

public interface UserService {

    UserDTO createUser(UserDTO userDTO);

    UserDTO updateUser(Long userId, UserDTO userDTO);

    UserDTO getUserById(Long userId);

    void deactivateUser(Long userId);

    PageDTO getUserList(Integer page, Integer size, String role, String status);
}
