package bitlab.springbootfirstfinal.services;

import bitlab.springbootfirstfinal.dto.CommentsDTO;
import bitlab.springbootfirstfinal.dto.UserDTO;
import bitlab.springbootfirstfinal.models.Comments;
import bitlab.springbootfirstfinal.models.User;

import java.util.List;

public interface CommentsService {
    List<CommentsDTO> commentsByTaskId(Long taskId);
    CommentsDTO addComment(CommentsDTO comment, Long taskId, UserDTO currentUser);
}
