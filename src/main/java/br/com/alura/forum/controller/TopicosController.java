package br.com.alura.forum.controller;


import java.net.URI;
import java.util.List;
import java.util.Optional;

import br.com.alura.forum.controller.dto.AtualizacaoTopicoForm;
import br.com.alura.forum.controller.dto.DetalheDoTopicoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.alura.forum.controller.dto.TopicoDto;
import br.com.alura.forum.controller.form.TopicoForm;
import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;

import javax.transaction.Transactional;
import javax.validation.Valid;

@RestController
@RequestMapping("/topicos")
public class TopicosController {

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @GetMapping
    public Page<TopicoDto> lista(@RequestParam(required = false) String nomeCurso,
                                 @RequestParam int pagina, @RequestParam int qtd, @RequestParam String ordenacao) {
        /*Paginação com ordenação
        * ENDPOINT EXEMPLO: http://localhost:8080/topicos?pagina=0&qtd=5&ordenacao=titulo
        * */
        Pageable paginacao = PageRequest.of(pagina, qtd, Sort.Direction.ASC, ordenacao);
        if (nomeCurso == null) {
            Page<Topico> topicos = topicoRepository.findAll(paginacao);
            return TopicoDto.converter(topicos);
        } else {
            Page<Topico> topicos = topicoRepository.findByCursoNome(nomeCurso, paginacao);
            return TopicoDto.converter(topicos);
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<TopicoDto> cadastrar(@RequestBody @Valid TopicoForm form, UriComponentsBuilder uriBuilder) {
        Topico topico = form.converter(cursoRepository);
        topicoRepository.save(topico);
        URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(uri).body(new TopicoDto(topico));
    }


    @GetMapping("/{id}")
    public ResponseEntity<DetalheDoTopicoDto> detalhar(@PathVariable Long id) {
        Optional<Topico> topico = this.topicoRepository.findById(id);
        if (topico.isPresent()) {
            return ResponseEntity.ok(new DetalheDoTopicoDto(topico.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<TopicoDto> update(@PathVariable Long id, @RequestBody @Valid AtualizacaoTopicoForm form) {
        Optional<Topico> optional = this.topicoRepository.findById(id);
        if (optional.isPresent()) {
            Topico topico = form.update(id, topicoRepository);
            return ResponseEntity.ok(new TopicoDto(topico));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity remove(@PathVariable Long id) {
        Optional<Topico> optional = this.topicoRepository.findById(id);
        if (optional.isPresent()) {
            this.topicoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

}
