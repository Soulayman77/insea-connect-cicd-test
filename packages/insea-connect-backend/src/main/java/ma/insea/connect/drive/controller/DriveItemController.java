package ma.insea.connect.drive.controller;


import lombok.RequiredArgsConstructor;
import ma.insea.connect.drive.model.DriveItem;
import ma.insea.connect.drive.model.File;
import ma.insea.connect.drive.model.Folder;
import ma.insea.connect.drive.service.DriveItemServiceImpl;
import ma.insea.connect.user.DegreePath;
import ma.insea.connect.user.User;
import ma.insea.connect.utils.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/drive")
@RequiredArgsConstructor
public class DriveItemController {

    private final Functions functions;

    @Autowired
    private DriveItemServiceImpl driveItemService;
    @Autowired
    private ma.insea.connect.drive.service.FolderServiceImpl folderService;
    @Autowired
    private ma.insea.connect.drive.repository.DegreePathRepository degreePathRepository;



    @GetMapping("/degreePaths/{degreePathCode}/items")
    public ResponseEntity<List<DriveItem>> getDriveItems(@PathVariable Long degreePathCode) {
        if (driveItemService.getDriveItems(degreePathCode) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(driveItemService.getDriveItems(degreePathCode));
    }

    @PreAuthorize("hasRole('CLASS_REP')")
    @PostMapping("/degreePaths/{degreePathCode}/folder")
    public ResponseEntity<Folder> CreateDriveItem(@PathVariable Long degreePathCode, @RequestBody Folder folder) {
        if (driveItemService.createDriveItem(degreePathCode, folder) == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(folder);
    }

    @PreAuthorize("hasRole('CLASS_REP')")
    @PostMapping("/degreePaths/{degreePathCode}/upload")
    public File handleFileUpload(@RequestParam("file") MultipartFile file,@PathVariable Long degreePathCode) {
        User user=functions.getConnectedUser();
        DegreePath degreePath = degreePathRepository.findById(degreePathCode).get();
        if(!user.getDegreePath().equals(degreePath)){
            return null;
        }
        if (file.isEmpty()) {return null;}

        File fileObj = new File();
        fileObj.setFileUrl(functions.uploadFile(file));
        fileObj.setName(file.getOriginalFilename());
        fileObj.setSize(file.getSize());
        fileObj.setMimeType(file.getContentType());
        fileObj.setCreatedAt(LocalDateTime.now());
        fileObj.setParent(null);

        return fileObj;

    }

    @PreAuthorize("hasRole('CLASS_REP')")
    @PostMapping("/{folderId}/upload")
    public File handleFileUploadOnFolder(@RequestParam("file") MultipartFile file, @PathVariable Long folderId) {
        if (file.isEmpty()) {return null;}

        File fileObj = new File();
        fileObj.setFileUrl(functions.uploadFile(file));
        fileObj.setName(file.getOriginalFilename());
        fileObj.setSize(file.getSize());
        fileObj.setMimeType(file.getContentType());
        fileObj.setCreatedAt(LocalDateTime.now());
        Folder folder = folderService.getFolderById(folderId);
        fileObj.setParent(folder);
        return fileObj;

    }
}