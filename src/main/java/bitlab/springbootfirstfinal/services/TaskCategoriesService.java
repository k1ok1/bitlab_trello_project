package bitlab.springbootfirstfinal.services;

import bitlab.springbootfirstfinal.dto.TaskCategoriesDTO;
import bitlab.springbootfirstfinal.models.TaskCategories;

import java.util.List;

public interface TaskCategoriesService {
    List<TaskCategoriesDTO> allCategories();
    void addToListCategory(TaskCategories taskCategory);
    void deleteFromListCategory(Long categoryId);
    void editCategory(TaskCategories taskCategory);
}
