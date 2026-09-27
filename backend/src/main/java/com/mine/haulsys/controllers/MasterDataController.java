package com.mine.haulsys.controllers;

import com.mine.haulsys.dto.AccountDto;
import com.mine.haulsys.dto.ContactDto;
import com.mine.haulsys.dto.JournalDto;
import com.mine.haulsys.dto.ProductDto;
import com.mine.haulsys.services.MasterDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataService masterDataService;

    @PostMapping("/contacts")
    public ResponseEntity<ContactDto> createContact(@Valid @RequestBody ContactDto contactDto) {
        return ResponseEntity.ok(masterDataService.createContact(contactDto));
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDto>> findAllContacts() {
        return ResponseEntity.ok(masterDataService.findAllContacts());
    }

    @GetMapping("/contacts/{id}")
    public ResponseEntity<ContactDto> findContactById(@PathVariable Long id) {
        return ResponseEntity.ok(masterDataService.findContactById(id));
    }

    @PutMapping("/contacts/{id}")
    public ResponseEntity<ContactDto> updateContact(@PathVariable Long id, @Valid @RequestBody ContactDto contactDto) {
        return ResponseEntity.ok(masterDataService.updateContact(id, contactDto));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(masterDataService.createProduct(productDto));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> findAllProducts() {
        return ResponseEntity.ok(masterDataService.findAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> findProductById(@PathVariable Long id) {
        return ResponseEntity.ok(masterDataService.findProductById(id));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto accountDto) {
        return ResponseEntity.ok(masterDataService.createAccount(accountDto));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> findAllAccounts() {
        return ResponseEntity.ok(masterDataService.findAllAccounts());
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountDto> findAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(masterDataService.findAccountById(id));
    }

    @PostMapping("/journals")
    public ResponseEntity<JournalDto> createJournal(@Valid @RequestBody JournalDto journalDto) {
        return ResponseEntity.ok(masterDataService.createJournal(journalDto));
    }

    @GetMapping("/journals")
    public ResponseEntity<List<JournalDto>> findAllJournals() {
        return ResponseEntity.ok(masterDataService.findAllJournals());
    }
}
