package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicemongodb.entity.ModuleMasterEntity;
import com.primesys.adminserviceserver.service.ModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/v2/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    public List<ModuleMasterEntity> getModules() {
        return moduleService.getAllModules();
    }

    @GetMapping("/{id}")
    public ModuleMasterEntity getModule(@PathVariable Long id) {
        return moduleService.getModuleById(id);
    }
}
