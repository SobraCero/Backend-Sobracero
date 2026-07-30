package sobracero.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sobracero.backend.services.FileService;

@RestController
@RequestMapping("/api/archivos")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/subir")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileService.uploadImage(file);
        return ResponseEntity.ok("Archivo subido con éxito: " + fileName);
    }
}