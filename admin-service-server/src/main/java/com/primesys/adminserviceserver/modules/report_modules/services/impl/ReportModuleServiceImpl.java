package com.primesys.adminserviceserver.modules.report_modules.services.impl;

import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.entity.ModuleMasterEntity;
import com.primesys.adminservicemongodb.entity.ReportModule;
import com.primesys.adminservicemongodb.model.UserReportModuleModel;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminservicemongodb.repository.ModuleMasterRepository;
import com.primesys.adminservicemongodb.repository.ReportModuleRepository;
import com.primesys.adminserviceserver.modules.report_modules.dtos.*;
import com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport.DivisionReportHierarchyResponseDTO;
import com.primesys.adminserviceserver.modules.report_modules.mapper.ReportModuleMapper;
import com.primesys.adminserviceserver.modules.report_modules.services.ReportModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportModuleServiceImpl implements ReportModuleService {

    private final ReportModuleRepository reportModuleRepository;
    private final DivisionLoginRepository divisionLoginRepository;
    private final ModuleMasterRepository moduleMasterRepository;

    @Override
    public List<ReportModuleResponseDTO> getUserReportModules() {

        List<ReportModule> reportModules = reportModuleRepository.findAll();

        Map<ObjectId, ReportModuleResponseDTO> map = new HashMap<>();

        for (ReportModule module : reportModules) {
            ReportModuleResponseDTO dto = ReportModuleMapper.toDTO(module);
            dto.setSubModules(new ArrayList<>());
            map.put(module.getId(), dto);
        }

        List<ReportModuleResponseDTO> roots = new ArrayList<>();

        for (ReportModule module : reportModules) {
            ReportModuleResponseDTO current = map.get(module.getId());

            if (module.getParentId() == null) {
                roots.add(current);
            } else {
                ReportModuleResponseDTO parent = map.get(module.getParentId());
                if (parent != null) {
                    parent.getSubModules().add(current);
                }
            }
        }

        Comparator<ReportModuleResponseDTO> comparator = Comparator.comparing(ReportModuleResponseDTO::getDisplayOrder);

        roots.sort(comparator);
        roots.forEach(r -> sortRecursively(r, comparator));

        return roots;
    }

    @Override
    public List<ReportModuleResponseDTO> getAllReportModule() {
        List<ReportModule> reportModules = reportModuleRepository.findAll();
        return reportModules.stream().map(ReportModuleMapper::toDTO).toList();
    }

    @Override
    public ReportModuleResponseDTO getModuleById(String moduleId) {
        ObjectId objectId = new ObjectId(moduleId);
        ReportModule reportModule = reportModuleRepository.findById(objectId)
                .orElseThrow(() -> new IllegalArgumentException("Module does not exists"));
        return ReportModuleMapper.toDTO(reportModule);
    }

    @Override
    public ReportModuleResponseDTO createReportModule(ReportModuleCreateDTO moduleCreateDTO) {
        if (reportModuleRepository.findByModuleName(moduleCreateDTO.moduleName()).isPresent()) {
            throw new IllegalArgumentException("Module already exists");
        }
        ReportModule reportModule = ReportModuleMapper.toEntity(moduleCreateDTO);
        ReportModule createModule = reportModuleRepository.save(reportModule);
        return ReportModuleMapper.toDTO(createModule);
    }

    @Override
    public ReportModuleResponseDTO updateReportModule(String moduleId, ReportModuleUpdateDTO dto) {

        if (!ObjectId.isValid(moduleId)) {
            throw new IllegalArgumentException("Invalid moduleId");
        }

        ObjectId objectId = new ObjectId(moduleId);

        ReportModule reportModule = reportModuleRepository.findById(objectId)
                .orElseThrow(() -> new IllegalArgumentException("Module does not exist"));

        if (dto.description() != null && !dto.description().isBlank()) {
            reportModule.setDescription(dto.description());
        }

        if (dto.moduleName() != null && !dto.moduleName().isBlank()) {
            reportModule.setModuleName(dto.moduleName());
        }

        if (dto.displayName() != null && !dto.displayName().isBlank()) {
            reportModule.setDisplayName(dto.displayName());
        }

        if (dto.displayOrder() != null) {
            reportModule.setDisplayOrder(dto.displayOrder());
        }

        ReportModule updated = reportModuleRepository.save(reportModule);

        return ReportModuleMapper.toDTO(updated);
    }

    @Override
    public Boolean destroyModule(String moduleId) {
        ObjectId rootId = new ObjectId(moduleId);
        if (!reportModuleRepository.existsById(rootId)) {
            throw new IllegalArgumentException("Module does not exist");
        }
        List<ReportModule> allModules = reportModuleRepository.findAll();

        Map<ObjectId, List<ObjectId>> treeMap = new HashMap<>();

        for (ReportModule module : allModules) {
            if (module.getParentId() != null) {
                treeMap.computeIfAbsent(module.getParentId(), k -> new ArrayList<>()).add(module.getId());
            }
        }
        List<ObjectId> toDelete = new ArrayList<>();
        collectIds(rootId, treeMap, toDelete);

        reportModuleRepository.deleteAllById(toDelete);
        return true;
    }

    @Override
    public DivisionReportHierarchyResponseDTO getDivisionHierarchyReportModules(String divisionId) {

        if (!divisionLoginRepository.existsById(divisionId)) {
            throw new IllegalArgumentException("Division does not exist");
        }

        // Get division + its children
        List<DivisionLoginEntity> divisions = divisionLoginRepository.findParentAndChildren(divisionId);

        // Map to hold all DTOs by divisionId
        Map<String, DivisionReportHierarchyResponseDTO> map = new HashMap<>();

        // Step 1: Create DTOs
        for (DivisionLoginEntity division : divisions) {

            DivisionReportHierarchyResponseDTO dto = new DivisionReportHierarchyResponseDTO();

            dto.setDivisionId(division.getId());
            dto.setName(division.getName());
            dto.setPath(division.getPath());
            dto.setSubDivisions(new ArrayList<>());

            dto.setModuleDetail(division.getReportModules() != null
                    ? division.getReportModules().stream().map(ReportModuleMapper::toReportModuleResponse).toList()
                    : new ArrayList<>());

            map.put(division.getId(), dto);
        }

        // Step 2: Build hierarchy (parent-child divisions)
        DivisionReportHierarchyResponseDTO root = null;

        for (DivisionLoginEntity division : divisions) {

            String currentId = division.getId();
            DivisionReportHierarchyResponseDTO current = map.get(currentId);

            if (currentId.equals(divisionId)) {
                root = current;
                continue;
            }

            String parentId = getParentIdFromPath(division.getPath());
            DivisionReportHierarchyResponseDTO parent = map.get(parentId);

            if (parent != null) {
                parent.getSubDivisions().add(current);
            }
        }

        return root;
    }

    @Override
    public String createDivisionHierarchyReportModule(String divisionId,
            List<DivisionReportModuleCreateDTO> reportModules) {

        if (!divisionLoginRepository.existsById(divisionId)) {
            throw new IllegalArgumentException("Division does not exist");
        }

        List<DivisionLoginEntity> divisions = divisionLoginRepository.findParentAndChildren(divisionId);

        for (DivisionLoginEntity division : divisions) {
            List<UserReportModuleModel> newModules = new ArrayList<>();
            for (DivisionReportModuleCreateDTO dto : reportModules) {
                newModules.add(ReportModuleMapper.toUserReportModuleModel(dto));
            }
            division.setReportModules(newModules);
        }

        divisionLoginRepository.saveAll(divisions);

        return "Updated " + divisions.size() + " divisions successfully";
    }

    @Override
    public String updateSingleDivisionReportModule(String divisionId, UserReportModuleModel updatedModule) {

        // DivisionLoginEntity division = divisionLoginRepository.findById(divisionId)
        // .orElseThrow(() -> new IllegalArgumentException("Division not found"));
        //

        ModuleMasterEntity masterEntity = moduleMasterRepository.findById(divisionId)
                .orElseThrow(() -> new IllegalArgumentException("No module"));

        List<ModuleMasterEntity> moduleMasterEntities = masterEntity.getSubModules();

        ModuleMasterEntity createModule = new ModuleMasterEntity();
        createModule.setModuleName("Keyman_Mid_Shift_Report");
        createModule.setDisplayOrder(8);
        createModule.setTypeId(20);
        createModule.setDisplayName("KeyMan Mid-Shift Report");
        moduleMasterEntities.add(createModule);
        masterEntity.setSubModules(moduleMasterEntities);

        moduleMasterRepository.save(masterEntity);

        log.info("{}", moduleMasterEntities);

        // boolean found = updateModule(
        // division.getReportModules(),
        // updatedModule
        // );

        // if (!found) {
        // throw new IllegalArgumentException("Module not found");
        // }

        // divisionLoginRepository.save(division);

        return "Module updated successfully";
    }

    private List<ReportModuleResponseDTO> buildTree(ObjectId parentId, Map<ObjectId, List<ReportModule>> parentMap) {

        List<ReportModule> children = parentMap.get(parentId);

        if (children == null)
            return List.of();

        return children.stream().map(module -> {
            ReportModuleResponseDTO dto = mapToDto(module);

            dto.setSubModules(buildTree(module.getId(), parentMap));

            return dto;
        }).toList();
    }

    private ReportModuleResponseDTO mapToDto(ReportModule module) {

        ReportModuleResponseDTO dto = new ReportModuleResponseDTO();

        dto.setId(module.getId().toHexString());
        dto.setParentId(module.getParentId() != null ? module.getParentId().toHexString() : null);
        dto.setModuleName(module.getModuleName());
        dto.setDescription(module.getDescription());
        dto.setDisplayName(module.getDisplayName());
        dto.setDisplayOrder(module.getDisplayOrder());
        dto.setTypeId(module.getTypeId());
        dto.setActive(module.getStatus());

        return dto;
    }

    private boolean updateModule(List<UserReportModuleModel> modules, UserReportModuleModel updatedModule) {

        if (modules == null)
            return false;

        for (UserReportModuleModel module : modules) {

            // if (module.getModuleId().equals(updatedModule.getModuleId())) {
            // module.setCustomDisplayName(updatedModule.getCustomDisplayName());
            // module.setActive(updatedModule.getActive());
            // module.setDisplayOrder(updatedModule.getDisplayOrder());
            // module.setSubModules(updatedModule.getSubModules());
            // return true;
            // }

            if (updateModule(module.getSubModules(), updatedModule)) {
                return true;
            }
        }

        return false;
    }

    private void collectIds(ObjectId current, Map<ObjectId, List<ObjectId>> treeMap, List<ObjectId> result) {

        result.add(current);

        List<ObjectId> children = treeMap.get(current);

        if (children != null) {
            for (ObjectId child : children) {
                collectIds(child, treeMap, result);
            }
        }
    }

    private void sortRecursively(ReportModuleResponseDTO node, Comparator<ReportModuleResponseDTO> comparator) {
        if (node.getSubModules() != null) {
            node.getSubModules().sort(comparator);
            for (ReportModuleResponseDTO child : node.getSubModules()) {
                sortRecursively(child, comparator);
            }
        }
    }

    /// method to create hierarchy

    private String getParentIdFromPath(String path) {

        String[] parts = path.split(",");

        List<String> ids = Arrays.stream(parts).filter(s -> !s.isEmpty()).toList();

        if (ids.size() < 2)
            return null;

        return ids.get(ids.size() - 2);
    }

    private List<UserReportModuleModel> mergeModules(List<UserReportModuleModel> existing,
            List<UserReportModuleModel> incoming) {
        Map<String, UserReportModuleModel> map = new LinkedHashMap<>();

        // if (existing != null) {
        // for (UserReportModuleModel m : existing) {
        // if (m.getModuleId() != null) {
        // map.put(m.getModuleId(), m);
        // }
        // }
        // }

        if (incoming != null) {
            for (UserReportModuleModel m : incoming) {

                // if (m.getModuleId() == null) {
                // throw new IllegalArgumentException("moduleId cannot be null");
                // }
                //
                // if (map.containsKey(m.getModuleId())) {
                // UserReportModuleModel old = map.get(m.getModuleId());
                //
                // old.setCustomDisplayName(m.getCustomDisplayName());
                // old.setActive(m.getActive());
                // old.setDisplayOrder(m.getDisplayOrder());
                //
                // old.setSubModules(
                // mergeModules(old.getSubModules(), m.getSubModules())
                // );
                //
                // } else {
                //
                // map.put(m.getModuleId(), m);
                // }
            }
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public ModuleMasterEntity updateModuleMaster(ModuleMasterCreateDTO moduleMasterCreateDTO) {
        ModuleMasterEntity masterEntity = moduleMasterRepository.findById(moduleMasterCreateDTO.moduleId())
                .orElseThrow(() -> new IllegalArgumentException("No such module exists"));

        ModuleMasterEntity subModule = new ModuleMasterEntity();
        subModule.setModuleName(moduleMasterCreateDTO.moduleName());
        subModule.setDisplayName(moduleMasterCreateDTO.displayName());
        subModule.setTypeId(moduleMasterCreateDTO.typeId());
        subModule.setDisplayOrder(masterEntity.getDisplayOrder());

        List<ModuleMasterEntity> subModules = masterEntity.getSubModules();
        subModules.add(subModule);

        moduleMasterRepository.save(masterEntity);

        return masterEntity;
    }

}

//
// {
// "_id" : ObjectId("673ae6fdc7de3b6aabc4d0cc"),
// "module_name" : "Exception_Report",
// "display_name" : "Exception Report",
// "display_order" : 1,
// "sub_modules" : [
// {
// "module_name" : "Keyman_report",
// "display_name" : "KeyMan Report",
// "display_order" : 1,
// "type_id" : 1
// },
// {
// "module_name" : "Patrolman_report",
// "display_name" : "Patrolman Report",
// "display_order" : 2,
// "type_id" : 2
// },
// {
// "module_name" : "SWM_report",
// "display_name" : "SWM Report",
// "display_order" : 3,
// "type_id" : 6
// },
// {
// "module_name" : "USFD_report",
// "display_name" : "USFD Report",
// "display_order" : 4,
// "type_id" : 3
// },
// {
// "module_name" : "Gatemitra_report",
// "display_name" : "Gatemitra Report",
// "display_order" : 5,
// "type_id" : 5
// },
// {
// "module_name" : "Mate_report",
// "display_name" : "Mate Report",
// "display_order" : 6,
// "type_id" : 4
// },
// {
// "module_name" : "Push_Trolley_report",
// "display_name" : "Push Trolley",
// "display_order" : 7,
// "type_id" : 13
// }
// ]
// },