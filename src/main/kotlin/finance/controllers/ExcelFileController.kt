package finance.controllers

import finance.services.ExcelFileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
@RequestMapping("/excel")
class ExcelFileController(private var excelFileService: ExcelFileService) {

    @GetMapping(path = ["/file/export"], produces = ["application/json"])
    fun exportExcelFile(): ResponseEntity<String> {
        excelFileService.processProtectedExcelFile("finance_db_master.xlsm")
        return ResponseEntity.ok("finished loading and saving excel file")
    }
}