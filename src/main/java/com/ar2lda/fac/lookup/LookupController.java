package com.ar2lda.fac.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LookupController {
    private final LookupService service;

    @GetMapping("/clientes/lookup")
    public Page<Map<String, Object>> clientes(@RequestParam MultiValueMap<String, String> params) {
        return service.find(false, params);
    }
    @GetMapping("/artigos/lookup")
    public Page<Map<String, Object>> artigos(@RequestParam MultiValueMap<String, String> params) {
        return service.find(true, params);
    }
}
