package com.erick.cursomc.services;

import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erick.cursomc.domain.ItemPedido;
import com.erick.cursomc.domain.PagamentoComBoleto;
import com.erick.cursomc.domain.Pedido;
import com.erick.cursomc.domain.enums.EstadoPagamento;
import com.erick.cursomc.repositories.ClienteRepository;
import com.erick.cursomc.repositories.ItemPedidoRepository;
import com.erick.cursomc.repositories.PagamentoRepository;
import com.erick.cursomc.repositories.PedidoRepository;
import com.erick.cursomc.repositories.ProdutoRepository;
import com.erick.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	
	@Autowired
	private PedidoRepository repo;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ProdutoRepository produtoRepository;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	@Autowired
	private ClienteRepository clienteRepository;
	
	@Autowired 
	private ClienteService clienteService;
	
	@Autowired
	private ProdutoService produtoService;
	
	@Autowired
	private EmailService emailService;
	
	public Pedido find(Integer id) {
		Optional<Pedido> obj = repo.findById(id);
		
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}
	
	@Transactional
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(1));

		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for(ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
			//produtoRepository.findById(ip.getProduto().getId()).orElseThrow(() -> new ObjectNotFoundException(
			//"Objeto não encontrado! Id: " + ip.getProduto().getId() + ", Tipo: " + Produto.class.getName())));
			//Optional<Produto> prd = produtoRepository.findById(ip.getProduto().getId()).get().getPreco();
			//ip.setPreco(.findById(ip.getProduto().getId()).get().getPreco());
			ip.setPreco(ip.getProduto().getPreco());

		}
		itemPedidoRepository.saveAll(obj.getItens());
		emailService.sendOrderConfirmationEmail(obj);

		return obj;
		
	}
	

}
