package com.mine.haulsys.services;

import com.mine.haulsys.models.*;
import com.mine.haulsys.models.enums.*;
import com.mine.haulsys.dto.*;
import com.mine.haulsys.exception.ResourceNotFoundException;
import com.mine.haulsys.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MasterDataService {

    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;

    // ---- CONTACTS ----
    public ContactDto createContact(ContactDto dto) {
        Contact contact = Contact.builder()
            .type(dto.getType())
            .name(dto.getName())
            .contactPerson(dto.getContactPerson())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .address(dto.getAddress())
            .taxId(dto.getTaxId())
            .build();
        return toContactDto(contactRepository.save(contact));
    }

    public List<ContactDto> findAllContacts() {
        return contactRepository.findAll().stream().map(this::toContactDto).collect(Collectors.toList());
    }

    public ContactDto findContactById(Long id) {
        return toContactDto(contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact", id)));
    }

    public ContactDto updateContact(Long id, ContactDto dto) {
        Contact c = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact", id));
        c.setType(dto.getType());
        c.setName(dto.getName());
        c.setContactPerson(dto.getContactPerson());
        c.setEmail(dto.getEmail());
        c.setPhone(dto.getPhone());
        c.setAddress(dto.getAddress());
        c.setTaxId(dto.getTaxId());
        return toContactDto(contactRepository.save(c));
    }

    // ---- PRODUCTS ----
    public ProductDto createProduct(ProductDto dto) {
        Product product = Product.builder()
            .name(dto.getName())
            .type(dto.getType())
            .sku(dto.getSku())
            .unitOfMeasure(dto.getUnitOfMeasure())
            .unitPrice(dto.getUnitPrice())
            .linkedAccountId(dto.getLinkedAccountId())
            .build();
        return toProductDto(productRepository.save(product));
    }

    public List<ProductDto> findAllProducts() {
        return productRepository.findAll().stream().map(this::toProductDto).collect(Collectors.toList());
    }

    public ProductDto findProductById(Long id) {
        return toProductDto(productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id)));
    }

    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product p = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        p.setName(dto.getName());
        p.setType(dto.getType());
        p.setSku(dto.getSku());
        p.setUnitOfMeasure(dto.getUnitOfMeasure());
        p.setUnitPrice(dto.getUnitPrice());
        p.setLinkedAccountId(dto.getLinkedAccountId());
        return toProductDto(productRepository.save(p));
    }

    // ---- ACCOUNTS (Chart of Accounts) ----
    public AccountDto createAccount(AccountDto dto) {
        Account account = Account.builder()
            .code(dto.getCode())
            .name(dto.getName())
            .type(dto.getType())
            .parentAccountId(dto.getParentAccountId())
            .build();
        return toAccountDto(accountRepository.save(account));
    }

    public List<AccountDto> findAllAccounts() {
        return accountRepository.findAll().stream().map(this::toAccountDto).collect(Collectors.toList());
    }

    public AccountDto findAccountById(Long id) {
        return toAccountDto(accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account", id)));
    }

    public AccountDto updateAccount(Long id, AccountDto dto) {
        Account a = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account", id));
        a.setCode(dto.getCode());
        a.setName(dto.getName());
        a.setType(dto.getType());
        a.setParentAccountId(dto.getParentAccountId());
        return toAccountDto(accountRepository.save(a));
    }

    // ---- JOURNALS ----
    public JournalDto createJournal(JournalDto dto) {
        Journal journal = Journal.builder()
            .name(dto.getName())
            .type(dto.getType())
            .build();
        return toJournalDto(journalRepository.save(journal));
    }

    public List<JournalDto> findAllJournals() {
        return journalRepository.findAll().stream().map(this::toJournalDto).collect(Collectors.toList());
    }

    public JournalDto findJournalById(Long id) {
        return toJournalDto(journalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Journal", id)));
    }

    // ---- MAPPERS ----
    private ContactDto toContactDto(Contact c) {
        ContactDto dto = new ContactDto();
        dto.setId(c.getId());
        dto.setType(c.getType());
        dto.setName(c.getName());
        dto.setContactPerson(c.getContactPerson());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setAddress(c.getAddress());
        dto.setTaxId(c.getTaxId());
        return dto;
    }

    private ProductDto toProductDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setType(p.getType());
        dto.setSku(p.getSku());
        dto.setUnitOfMeasure(p.getUnitOfMeasure());
        dto.setUnitPrice(p.getUnitPrice());
        dto.setLinkedAccountId(p.getLinkedAccountId());
        return dto;
    }

    private AccountDto toAccountDto(Account a) {
        AccountDto dto = new AccountDto();
        dto.setId(a.getId());
        dto.setCode(a.getCode());
        dto.setName(a.getName());
        dto.setType(a.getType());
        dto.setParentAccountId(a.getParentAccountId());
        return dto;
    }

    private JournalDto toJournalDto(Journal j) {
        JournalDto dto = new JournalDto();
        dto.setId(j.getId());
        dto.setName(j.getName());
        dto.setType(j.getType());
        return dto;
    }
}
