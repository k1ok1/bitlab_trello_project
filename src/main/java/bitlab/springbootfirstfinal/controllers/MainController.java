package bitlab.springbootfirstfinal.controllers;

import bitlab.springbootfirstfinal.dto.*;
import bitlab.springbootfirstfinal.mapper.CommentsMapper;
import bitlab.springbootfirstfinal.mapper.UserMapper;
import bitlab.springbootfirstfinal.models.*;
import bitlab.springbootfirstfinal.services.*;
import bitlab.springbootfirstfinal.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final FoldersService foldersService;
    private final TaskService taskService;
    private final TaskCategoriesService taskCategoriesService;
    private final CommentsService commentsService;
    private final TaskStatusService taskStatusService;
    private final UserService userService;
    private final CommentsMapper commentsMapper;
    private final UserMapper userMapper;

    @GetMapping(value = "/")
    public String index(Model model) {
        List<FoldersDTO> folders = new ArrayList<>();
        if (userService.getCurrentuser() != null) {
            folders = foldersService.allFolders(userService.getCurrentuser().getId());
        }
        model.addAttribute("folders", folders);
        model.addAttribute("currentUser", userService.getCurrentuser());
        return "index";
    }

    @PostMapping(value = "/addFolder")
    public String addFolder(Folders folder) {
        Folders newFolder = new Folders();
        if (folder != null) {
            newFolder = foldersService.addFolder(folder, userService.getCurrentuser().getId());
        }
        return (folder != null ? "redirect:/detailsFolder/" + newFolder.getFolderId() : "redirect:/");
    }

    @PostMapping(value = "/deleteFolder")
    public String deleteFolder(@RequestParam(name = "folderId") Long folderId) {
        foldersService.deleteFolder(folderId, userService.getCurrentuser().getId());
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/profile")
    public String profilePage(Model model) {
        model.addAttribute("currentUser", userService.getCurrentuser());
        return "profile";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping(value = "/auth")
    public String authPage(Model model) {
        model.addAttribute("currentUser", userService.getCurrentuser());
        return "auth";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping(value = "/signUp")
    public String signUpPage(Model model) {
        model.addAttribute("currentUser", userService.getCurrentuser());
        return "signUp";
    }

    @PostMapping(value = "/signUp")
    public String signUp(@RequestParam(name = "user_email") String userEmail,
            @RequestParam(name = "user_password") String userPassword,
            @RequestParam(name = "user_re_password") String userRePassword,
            @RequestParam(name = "user_full_name") String userFullName,
            Model model) {
        if (userPassword.equals(userRePassword)) {
            User newUser = new User();
            newUser.setEmail(userEmail);
            newUser.setPassword(userPassword);
            newUser.setFullName(userFullName);
            newUser = userService.newUser(newUser);
            if (newUser != null) {
                model.addAttribute("currentUser", userService.getCurrentuser());
                return "redirect:/signUp?success";
            } else {
                return "redirect:/signUp?errorUnknown";
            }
        } else {
            return "redirect:/signUp?errorPassword";
        }
    }

    @PostMapping(value = "/updatePassword")
    public String updatePassword(@RequestParam(name = "user_old_password") String oldPassword,
                                 @RequestParam(name = "user_new_password") String newPassword,
                                 @RequestParam(name = "user_new_re_password") String newRePassword) {
        if (newPassword.equals(newRePassword)) {
            User userUpdatedPassword = userService.updatePassword(userService.getCurrentuser(), oldPassword, newPassword);
            if (userUpdatedPassword != null){
                return "redirect:/profile?success";
            }
        }
        return "redirect:/profile?error";
    }

    @PostMapping(value = "/updateUserName")
    public String updateUserName(@RequestParam(name = "user_new_full_name") String fullName,
                                 Model model) {
        if (fullName != null) {
            User currentUser = userService.updateFullName(userService.getCurrentuser(), fullName);
            userService.updateUserData(fullName);
            model.addAttribute("currentUser", currentUser);
            return "redirect:/profile?success";
        }
        return "redirect:/profile?error";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/detailsFolder/{folderId}")
    public String detailsFolder(@PathVariable(name = "folderId") Long folderId,
            Model model) {
        FoldersDTO folder = foldersService.detailsFolder(folderId, userService.getCurrentuser().getId());

        List<TasksDTO> tasks = taskService.tasksList(folderId);
        model.addAttribute("tasks", tasks);

        List<TaskCategoriesDTO> taskCategories = foldersService.detailsTaskCategories(folderId);
        model.addAttribute("taskCategories", taskCategories);
        List<TaskCategoriesDTO> allCategories = taskCategoriesService.allCategories();
        allCategories.removeAll(taskCategories);
        model.addAttribute("allCategories", allCategories);

        List<TaskStatusDTO> taskStatusList = taskStatusService.allTaskStatus();
        model.addAttribute("taskStatusList", taskStatusList);

        model.addAttribute("currentUser", userService.getCurrentuser());

        if (folder != null) {
            model.addAttribute("folder", folder);
            return "detailsFolder";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping(value = "/addCategory")
    public String addCategoryToFolder(@RequestParam(name = "folderId") Long folderId,
                                      @RequestParam(name = "categoriesId") Long categoryId) {
        foldersService.addCategoryToFolder(folderId, categoryId);
        return "redirect:/detailsFolder/" + folderId;
    }

    @PostMapping(value = "/deleteCategory")
    public String deleteCategory(@RequestParam(name = "folderId") Long folderId,
                                 @RequestParam(name = "categoriesId") Long categoriesId) {
        foldersService.deleteCategoryFromFolder(folderId, categoriesId);
        return "redirect:/detailsFolder/" + folderId;
    }

    @GetMapping(value = "/detailsTask/{folderId}/{taskId}")
    public String detailsTask(@PathVariable(name = "folderId") Long folderId,
            @PathVariable(name = "taskId") Long taskId,
            Model model) {
        List<TasksDTO> allTasks = taskService.tasksList(folderId);
        model.addAttribute("allTasks", allTasks);

        TasksDTO task = taskService.task(taskId);
        if (task != null && userService.getCurrentuser() != null) {
            model.addAttribute("task", task);
            FoldersDTO folder = foldersService.detailsFolder(folderId, userService.getCurrentuser().getId());
            model.addAttribute("folder", folder);

            List<CommentsDTO> commentsByTaskId = commentsService.commentsByTaskId(taskId);
            model.addAttribute("commentsByTaskId", commentsByTaskId);

            List<TaskStatusDTO> allTaskStatus = taskStatusService.allTaskStatus();
            model.addAttribute("allTaskStatus", allTaskStatus);

            boolean canUpdateTask = true;
            if (task.getTaskStatus().getTaskStatusId() > 2) {
                canUpdateTask = false;
            } else {
                canUpdateTask = true;
            }
            model.addAttribute("canUpdateTask", canUpdateTask);
            model.addAttribute("currentUser", userService.getCurrentuser());
            return "detailsTask";
        }
        return "auth";
    }

    @PostMapping(value = "/addTask")
    public String addTask(Tasks task,
                          @RequestParam(name = "folderId") Long folderId,
                          @RequestParam(name = "taskStatusId") TaskStatus taskStatusId,
                          Model model) {
        taskService.addTask(task, folderId, taskStatusId);
        return "redirect:/detailsFolder/" + folderId;
    }

    @PostMapping(value = "/updateTask")
    public String detailsTask(Tasks task,
                              @RequestParam(name = "folderId") Long folderId,
                              Model model) {
        if (taskService.updateTask(task, folderId) != null) {
            return "redirect:/detailsFolder/" + folderId;
        }
        return "redirect:/";
    }

    @PostMapping(value = "/deleteTask")
    public String deleteTask(@RequestParam(name = "taskId") Long taskId,
                             @RequestParam(name = "folderId") Long folderId) {
        taskService.deleteTask(taskId);
        return "redirect:/detailsFolder/" + folderId;
    }

    @PostMapping(value = "/addComment")
    public String addComment(Comments comment,
                             @RequestParam(name = "folderId") Long folderId,
                             @RequestParam(name = "taskId") Long taskId,
                             Model model) {
        commentsService.addComment(comment, taskId, userService.getCurrentuser());
        return "redirect:/detailsTask/" + folderId + "/" + taskId;
    }

    @PostMapping(value = "/editFolderTitle")
    public String editFolderTitle(@RequestParam(name = "folderId") Long folderId,
                                  @RequestParam(name = "folderTitle") String folderTitle,
                                  Model model) {
        Folders folder = foldersService.editFolderTitle(folderId, folderTitle);
        model.addAttribute("folder", folder);
        return "redirect:/detailsFolder/" + folder.getFolderId();
    }


    @GetMapping(value = "/allCategories")
    public String allCategories(Model model) {
        List<TaskCategoriesDTO> allCategories = taskCategoriesService.allCategories();
        model.addAttribute("allCategories", allCategories);

        model.addAttribute("currentUser", userService.getCurrentuser());
        return "categories";
    }

    @PostMapping(value = "/addToListCategory")
    public String addToListCategory(TaskCategories taskCategory) {
        taskCategoriesService.addToListCategory(taskCategory);
        return "redirect:/allCategories";
    }

    @PostMapping(value = "/deleteFromListCategory")
    public String deleteFromListCategory(@RequestParam(name = "categoriesId") Long categoriesId) {
        taskCategoriesService.deleteFromListCategory(categoriesId);
        return "redirect:/allCategories";
    }

    @PostMapping(value = "/editCategory")
    public String editCategory(TaskCategories taskCategory) {
        taskCategoriesService.editCategory(taskCategory);
        return "redirect:/allCategories";
    }

}
