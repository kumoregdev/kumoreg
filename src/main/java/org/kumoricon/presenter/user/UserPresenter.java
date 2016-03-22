package org.kumoricon.presenter.user;

import org.kumoricon.model.role.Role;
import org.kumoricon.model.role.RoleRepository;
import org.kumoricon.model.user.User;
import org.kumoricon.model.user.UserRepository;
import org.kumoricon.view.user.UserEditWindow;
import org.kumoricon.view.user.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserPresenter {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public UserPresenter() {
    }

    public void userSelected(UserView view, User user) {
        if (user != null) {
            view.navigateTo(view.VIEW_NAME + "/" + user.getId().toString());
            view.showUser(user, getAvailableRoles());
        }
    }

    public void userSelected(UserView view, Integer id) {
        if (id != null) {
            User user = userRepository.findOne(id);
            userSelected(view, user);
        }
    }

    public void addNewUser(UserView view) {
        view.navigateTo(view.VIEW_NAME);
        User user = new User();
        user.setPassword(user.DEFAULT_PASSWORD);
        view.showUser(user, roleRepository.findAll());
    }

    public void cancelUser(UserEditWindow window) {
        UserView view = window.getParentView();
        view.navigateTo(UserView.VIEW_NAME);
        window.close();
        view.clearSelection();
    }

    public void saveUser(UserEditWindow window, User user) {
        UserView view = window.getParentView();
        userRepository.save(user);
        window.close();
        view.navigateTo(UserView.VIEW_NAME);
        showUserList(view);
    }

    public void showUserList(UserView view) {
        List<User> users = userRepository.findAll();
        view.afterSuccessfulFetch(users);
    }

    public void navigateToUser(UserView view, String parameters) {
        if (parameters != null) {
            Integer id = Integer.parseInt(parameters);
            User user = userRepository.findOne(id);
//            List<Role> roles = roleRepository.findAll();
            view.selectUser(user);
        }
    }

    public void resetPassword(UserEditWindow window, User user) {
        UserView view = window.getParentView();
        user.setPassword("password");
        try {
            userRepository.save(user);
            view.notify("Password reset for " + user.getUsername());
            window.close();
        } catch (Exception e) {
            view.notifyError(e.getMessage());
        }
    }

    public List<Role> getAvailableRoles() { return roleRepository.findAll(); }

    public UserRepository getUserRepository() { return userRepository; }
    public void setUserRepository(UserRepository userRepository) { this.userRepository = userRepository; }

    public RoleRepository getRoleRepository() { return roleRepository; }
    public void setRoleRepository(RoleRepository roleRepository) { this.roleRepository = roleRepository; }
}
