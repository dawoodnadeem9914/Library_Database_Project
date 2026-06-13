package com.unimas.library.service;

import com.unimas.library.entity.Member;
import com.unimas.library.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Member module: CRUD + paginated search + unique email/matric validation. */
@Service
@Transactional
public class MemberService {

    public static final int PAGE_SIZE = 8;

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public Page<Member> page(String keyword, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("fullName").ascending());
        if (keyword == null || keyword.isBlank()) return memberRepository.findAll(pageable);
        return memberRepository.search(keyword.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id));
    }

    public Member create(Member member) {
        validateUnique(member, null);
        Member saved = memberRepository.save(member);
        return saved;
    }

    public Member update(Long id, Member form) {
        Member existing = findById(id);
        validateUnique(form, id);
        existing.setFullName(form.getFullName());
        existing.setEmail(form.getEmail());
        existing.setPhone(form.getPhone());
        existing.setMatricNo(form.getMatricNo());
        existing.setMemberType(form.getMemberType());
        if (form.getPhotoUrl() != null && !form.getPhotoUrl().isBlank()) {
            existing.setPhotoUrl(form.getPhotoUrl());
        }
        Member saved = memberRepository.save(existing);
        return saved;
    }

    public void delete(Long id) {
        Member member = findById(id);
        if (!member.getLoans().isEmpty()) {
            throw new IllegalStateException(
                "Cannot delete " + member.getFullName() + " - loan records exist for this member.");
        }
        memberRepository.delete(member);
    }

    private void validateUnique(Member m, Long selfId) {
        memberRepository.findByEmail(m.getEmail()).ifPresent(other -> {
            if (selfId == null || !other.getId().equals(selfId))
                throw new IllegalStateException("Email " + m.getEmail() + " is already registered.");
        });
        memberRepository.findByMatricNo(m.getMatricNo()).ifPresent(other -> {
            if (selfId == null || !other.getId().equals(selfId))
                throw new IllegalStateException("Matric number " + m.getMatricNo() + " is already registered.");
        });
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll(Sort.by("fullName").ascending());
    }

    @Transactional(readOnly = true)
    public long count() { return memberRepository.count(); }

    @Transactional(readOnly = true)
    public List<Member> latest() { return memberRepository.findTop5ByOrderByCreatedAtDesc(); }

    @Transactional(readOnly = true)
    public long newThisMonth() {
        java.time.LocalDateTime monthStart = java.time.YearMonth.now().atDay(1).atStartOfDay();
        return memberRepository.countByCreatedAtAfter(monthStart);
    }
}
