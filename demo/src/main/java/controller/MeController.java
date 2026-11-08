package controller;

import dto.MeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.CurrentUserService;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final CurrentUserService currentUser;

    public MeController(CurrentUserService currentUser) {
        this.currentUser = currentUser;
    }

    @GetMapping
    public MeResponse me() {
        var user = currentUser.requireCurrentUser();
        return new MeResponse(user.getUsername(), user.getRole());
    }
}
