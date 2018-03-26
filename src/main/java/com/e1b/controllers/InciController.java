package com.e1b.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.e1b.entities.Incidencia;
import com.e1b.entities.Operario;
import com.e1b.services.InciService;
import com.e1b.services.OperariosService;

@Controller
public class InciController {

	@Autowired
	InciService inciService;

	@Autowired
	OperariosService opService;

	private final List<SseEmitter> emitters = new ArrayList<>();

	@RequestMapping(value = "/incidencias/list", method = RequestMethod.GET)
	public String list(Model model, Principal principal, Pageable pageable) {
		String username = principal.getName();
		Operario o = opService.findByUsername(username);
		Page<Incidencia> incidencias = new PageImpl<Incidencia>(new LinkedList<Incidencia>());
		incidencias = inciService.getIncidenciasByUser(o, pageable);
		model.addAttribute("inciList", incidencias.getContent());
		model.addAttribute("page", incidencias);
		return "/incidencias/list";
	}

	public List<SseEmitter> getEmitters() {
		return emitters;
	}

	public SseEmitter getLatestEmitter() {
		return (emitters.isEmpty()) ? null : emitters.get(emitters.size() - 1);
	}

	@GetMapping("/getEmitter")
	public SseEmitter getKafkaMessages() {
		SseEmitter emitter = new SseEmitter();
		emitters.add(emitter);
		emitter.onCompletion(new Runnable() {
			@Override
			public void run() {
				emitters.remove(emitter);
			}
		});
		emitter.onTimeout(new Runnable() {
			@Override
			public void run() {
				emitters.remove(emitter);
			}
		});
		return emitter;
	}

}