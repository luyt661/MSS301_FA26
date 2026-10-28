package fu.de181018.post.service;

import fu.de181018.post.dto.PageDTO;
import fu.de181018.post.dto.PostDTO;
import fu.de181018.post.dto.PostDetailDTO;

public interface PostService {

    PostDTO createPost(PostDTO postDTO);

    PostDTO updatePost(Long postId, PostDTO postDTO);

    PostDetailDTO getPostDetail(Long postId);

    void cancelPost(Long postId);

    PageDTO getPostList(Integer page, Integer size, String advertiserName, String status);
}
