/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.gob.edomex.sicapem.controller.view;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import mx.gob.edomex.sicapem.bo.PrivilegiosBO;
import mx.gob.edomex.sicapem.model.CustomMenuUsuario;
import mx.gob.edomex.sicapem.model.CustomMenuUsuarioPK;
import mx.gob.edomex.sicapem.model.Modulo;
import mx.gob.edomex.sicapem.model.Pantalla;
import mx.gob.edomex.sicapem.model.ServidorPublico;
import mx.gob.edomex.sicapem.model.UsuarioServidorPublico;
import mx.gob.edomex.sicapem.repository.CustomMenuUsuarioRepository;
import mx.gob.edomex.sicapem.repository.ModuloRepository;
import mx.gob.edomex.sicapem.repository.PantallaRepository;
import mx.gob.edomex.sicapem.repository.ServidorPublicoRepository;
import mx.gob.edomex.sicapem.repository.UnidadOrganizacionalRepository;
import mx.gob.edomex.sicapem.repository.UsuarioServidorPublicoRepository;
import mx.gob.edomex.sicapem.util.FacesUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 *
 * @author rhdtp
 */
@ManagedBean
@ViewScoped
public class UsuariosView extends SpringBeanAutowiringSupport implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(UsuariosView.class);

    private static final SortOrder DEFAULT_SORT_ORDER = SortOrder.ASCENDING;
    private static final String DEFAULT_SORT_FIELD = "idServidorPublico";

    @Autowired
    private transient CustomMenuUsuarioRepository customMenuUsuarioRepository;
    @Autowired
    private transient ModuloRepository moduloRepository;
    @Autowired
    private transient PantallaRepository pantallaRepository;
    @Autowired
    private transient ServidorPublicoRepository servidorPublicoRepository;
    @Autowired
    private transient UsuarioServidorPublicoRepository usuarioServidorPublicoRepository;
    @Autowired
    private transient UnidadOrganizacionalRepository unidadOrganizacionalRepository;

    private LazyDataModel<UsuarioServidorPublico> usuariosLazy;
    private ServidorPublico servidorPublico = new ServidorPublico();
    private UsuarioServidorPublico usuarioServidorPublico;
    private List<CustomMenuUsuario> privilegios;
    private Modulo modulo;
    private Pantalla pantalla;
    private TreeNode root;
    private String tmpPss;
    private String tmpPssBK;
    private boolean editar;

    @PostConstruct
    public void init() {
        usuarioServidorPublico = new UsuarioServidorPublico();
        usuarioServidorPublico.setServidorPublico(servidorPublico);
        usuariosLazy = new LazyDataModel<UsuarioServidorPublico>() {

            @Override
            public Object getRowKey(UsuarioServidorPublico usp) {
                return usp.getIdServidorPublico();
            }

            @Override
            public UsuarioServidorPublico getRowData(String rowKey) {
                Long rowId = Long.valueOf(rowKey);
                @SuppressWarnings("unchecked")
                List<UsuarioServidorPublico> usps = (List<UsuarioServidorPublico>) super.getWrappedData();
                return usps.stream().filter(usp -> usp.getIdServidorPublico().equals(rowId)).findAny().orElse(null);
            }

            @Override
            public List<UsuarioServidorPublico> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
                Sort sort = new Sort(getDirection(DEFAULT_SORT_ORDER), DEFAULT_SORT_FIELD);
                if (multiSortMeta != null) {
                    List<Sort.Order> orders = multiSortMeta.stream()
                            .map(m -> new Sort.Order(getDirection(m.getSortOrder() != null ? m.getSortOrder() : DEFAULT_SORT_ORDER),
                            m.getSortField()))
                            .collect(Collectors.toList());
                    sort = new Sort(orders);
                }
                return filterAndSort(first, pageSize, filters, sort);
            }

            @Override
            public List<UsuarioServidorPublico> load(int first, int pageSize, String sortField, SortOrder sortOrder,
                    Map<String, Object> filters) {
                Sort sort = null;
                if (sortField != null) {
                    sort = new Sort(getDirection(sortOrder != null ? sortOrder : DEFAULT_SORT_ORDER), sortField);
                } else if (DEFAULT_SORT_FIELD != null) {
                    sort = new Sort(getDirection(sortOrder != null ? sortOrder : DEFAULT_SORT_ORDER), DEFAULT_SORT_FIELD);
                }
                return filterAndSort(first, pageSize, filters, sort);
            }

            private List<UsuarioServidorPublico> filterAndSort(int first, int pageSize, Map<String, Object> filters, Sort sort) {
                Map<String, String> filtersMap = filters.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                Page<UsuarioServidorPublico> page = usuarioServidorPublicoRepository.findAll(getFilterSpecification(filtersMap), new PageRequest(first / pageSize, pageSize, sort));
                this.setRowCount(((Number) page.getTotalElements()).intValue());
                this.setWrappedData(page.getContent());
                return page.getContent();
            }

            private Sort.Direction getDirection(SortOrder order) {
                switch (order) {
                    case ASCENDING:
                        return Sort.Direction.ASC;
                    case DESCENDING:
                        return Sort.Direction.DESC;
                    case UNSORTED:
                    default:
                        return null;
                }
            }

            private Specification<UsuarioServidorPublico> getFilterSpecification(Map<String, String> filterValues) {
                return (Root<UsuarioServidorPublico> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
                    Optional<Predicate> predicate = filterValues.entrySet().stream()
                            .filter(v -> v.getValue() != null && v.getValue().length() > 0)
                            .map(entry -> {
                                Path<?> path = root;
                                String key = entry.getKey();
                                if (entry.getKey().contains(".")) {
                                    String[] splitKey = entry.getKey().split("\\.");
                                    path = root.join(splitKey[0]);
                                    key = splitKey[1];
                                }

                                if (key.contains("unidadOrganizacional")) {
                                    String[] splitKey = entry.getKey().split("\\.");
                                    key = splitKey[splitKey.length - 1];
                                    path = root.join("servidorPublico").join("unidadOrganizacional");
                                    return builder.like(builder.upper(path.get(key).as(String.class)), "%" + entry.getValue().toUpperCase() + "%");
                                }
                                return builder.like(builder.upper(path.get(key).as(String.class)), "%" + entry.getValue().toUpperCase() + "%");
                            })
                            .collect(Collectors.reducing((a, b) -> builder.and(a, b)));
                    return predicate.orElseGet(() -> alwaysTrue(builder));
                };
            }

            private Predicate alwaysTrue(CriteriaBuilder builder) {
                return builder.isTrue(builder.literal(true));
            }
        };
    }

    public void guardarSP() {
        try {

            servidorPublico.setIdServidorPublico(servidorPublicoRepository.getMaxId() + 1);

            servidorPublicoRepository.save(servidorPublico);

            usuarioServidorPublico.setServidorPublico(servidorPublico);

            FacesUtils.msgInfo(FacesUtils.MSG_EXITO, "Servidor Público guardado");
        } catch (Exception e) {
            LOG.error("Error: {}", e);
            FacesUtils.msgError(FacesUtils.MSG_ERROR, "Servidor Público no guardado");
        }
    }

    public void cancelarSP() {
        servidorPublico = new ServidorPublico();
    }

    public void guardarUsuario() {

        try {

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String passEncode = passwordEncoder.encode(tmpPss);

            if (editar) {

                if (!tmpPssBK.equals(passEncode)) {
                    usuarioServidorPublico.setPassword(passwordEncoder.encode(tmpPss));
                }

                usuarioServidorPublicoRepository.save(usuarioServidorPublico);

                FacesUtils.msgInfo(FacesUtils.MSG_EXITO, "Usuario actualizado");

                this.limpiaCampos();

            } else {
                UsuarioServidorPublico usp = usuarioServidorPublicoRepository.findByUsuario(usuarioServidorPublico.getUsuario());

                if (usp == null) {

                    usuarioServidorPublico.setPassword(passEncode);
                    usuarioServidorPublico.setIdServidorPublico(usuarioServidorPublico.getServidorPublico().getIdServidorPublico());
                    usuarioServidorPublicoRepository.save(usuarioServidorPublico);

                    FacesUtils.msgInfo(FacesUtils.MSG_EXITO, "Usuario guardado");

                    this.limpiaCampos();

                } else {
                    FacesUtils.msgWarning(FacesUtils.MSG_ALERTA, "Nombre de usuario ya regristado");
                }

            }

        } catch (Exception e) {
            LOG.error("Error: {}", e);
            FacesUtils.msgError(FacesUtils.MSG_ERROR, "Usuario no guardado");
        }

    }

    public void limpiaServidorPublico() {
        usuarioServidorPublico = new UsuarioServidorPublico();
    }

    public void limpiaCampos() {
        usuarioServidorPublico = new UsuarioServidorPublico();
        servidorPublico = new ServidorPublico();
        usuarioServidorPublico.setServidorPublico(servidorPublico);
        editar = false;
        tmpPss = "";
        tmpPssBK = "";
    }

    public void cancelarUsuario() {
        usuarioServidorPublico = new UsuarioServidorPublico();
    }

    public void cargaArbol(UsuarioServidorPublico usp) {
        usuarioServidorPublico = usp;

        root = new DefaultTreeNode("Privilegio de Módulos", null);

        for (Modulo md : moduloRepository.findAll()) {
            TreeNode node0 = new DefaultTreeNode(md, root);

            for (Pantalla pt : pantallaRepository.findAllByModulo(md)) {

                PrivilegiosBO pBO = new PrivilegiosBO();
                pBO.setPantalla(pt);
                pBO.setActive(false);

                for (CustomMenuUsuario cmu : customMenuUsuarioRepository.findAllByUsuarioServidorPublicoUsuarioAndPantallaIdIdModulo(usuarioServidorPublico.getUsuario(), md.getIdModulo())) {
                    if (Objects.equals(cmu.getPantalla().getId().getIdModulo(), pt.getId().getIdModulo()) && Objects.equals(cmu.getId().getIdPantalla(), pt.getId().getIdPantalla())) {
                        pBO.setActive(true);
                    }
                }

                TreeNode node00 = new DefaultTreeNode(pBO, node0);
            }

        }
    }

    public void privilegioEvent(PrivilegiosBO pBO) {

        CustomMenuUsuario customMenuUsuario = new CustomMenuUsuario();
        customMenuUsuario.setId(new CustomMenuUsuarioPK());
        customMenuUsuario.getId().setIdModulo(pBO.getPantalla().getModulo().getIdModulo());
        customMenuUsuario.getId().setIdPantalla(pBO.getPantalla().getId().getIdPantalla());
        customMenuUsuario.getId().setIdRol(usuarioServidorPublico.getRol().getIdRol());
        customMenuUsuario.getId().setIdServidorPublico(usuarioServidorPublico.getIdServidorPublico());
        customMenuUsuario.setPantalla(pBO.getPantalla());
        customMenuUsuario.setStatus(true);
        customMenuUsuario.setUsuarioServidorPublico(usuarioServidorPublico);

        if (pBO.isActive()) {
            customMenuUsuarioRepository.save(customMenuUsuario);
        } else {
            customMenuUsuarioRepository.delete(customMenuUsuario);
        }

        FacesUtils.msgInfo(FacesUtils.MSG_EXITO, "Privilegio: " + pBO.getPantalla().getNombrePantalla() + " : " + (pBO.isActive() ? "Activo" : "Inactivo"));
    }

    public void editUsuario(UsuarioServidorPublico usp) {
        usuarioServidorPublico = usp;
        servidorPublico = usp.getServidorPublico();
        tmpPssBK = usp.getPassword();
        editar = true;
        //privilegios = (List<CustomMenuUsuario>) customMenuUsuarioRepository.findAllByUsuarioServidorPublicoUsuario(usp.getUsuario());
    }

    public void eliminarUsuario(UsuarioServidorPublico usp) {
        try {
            usuarioServidorPublicoRepository.delete(usp);
            FacesUtils.msgInfo(FacesUtils.MSG_EXITO, "Usuario eliminado");
        } catch (Exception e) {
            LOG.error("Error: {}", e);
            FacesUtils.msgError(FacesUtils.MSG_ERROR, "Usuario no eliminado");
        }
    }

    public void elminarServidor(UsuarioServidorPublico usp) {
        try {
            servidorPublicoRepository.delete(usp.getIdServidorPublico());
            FacesUtils.msgInfo(FacesUtils.MSG_EXITO, "El Servidor Público se a eliminado");
        } catch (Exception e) {
            LOG.error("Error: {}", e);
            FacesUtils.msgError(FacesUtils.MSG_ERROR, "No se pudo eliminar el Servidor Público");
        }
    }

    public void addPrivilegio() {
        CustomMenuUsuario customMenuUsuario = new CustomMenuUsuario();
        customMenuUsuario.setId(new CustomMenuUsuarioPK());
        customMenuUsuario.getId().setIdModulo(modulo.getIdModulo());
        customMenuUsuario.getId().setIdPantalla(pantalla.getId().getIdPantalla());
        customMenuUsuario.getId().setIdRol(usuarioServidorPublico.getRol().getIdRol());
        customMenuUsuario.getId().setIdServidorPublico(usuarioServidorPublico.getIdServidorPublico());
        customMenuUsuario.setPantalla(pantalla);
        customMenuUsuario.setStatus(true);
        customMenuUsuario.setUsuarioServidorPublico(usuarioServidorPublico);

        privilegios.add(customMenuUsuario);

        FacesUtils.msgInfo(FacesUtils.MSG_EXITO, "Privilegio agregado");

    }

    public UsuarioServidorPublico getUsuarioServidorPublico() {
        return usuarioServidorPublico;
    }

    public void setUsuarioServidorPublico(UsuarioServidorPublico usuarioServidorPublico) {
        this.usuarioServidorPublico = usuarioServidorPublico;
    }

    public List<CustomMenuUsuario> getPrivilegios() {
        return privilegios;
    }

    public void setPrivilegios(List<CustomMenuUsuario> privilegios) {
        this.privilegios = privilegios;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }

    public Pantalla getPantalla() {
        return pantalla;
    }

    public void setPantalla(Pantalla pantalla) {
        this.pantalla = pantalla;
    }

    public TreeNode getRoot() {
        return root;
    }

    public LazyDataModel<UsuarioServidorPublico> getUsuariosLazy() {
        return usuariosLazy;
    }

    public void setUsuariosLazy(LazyDataModel<UsuarioServidorPublico> usuariosLazy) {
        this.usuariosLazy = usuariosLazy;
    }

    public ServidorPublico getServidorPublico() {
        return servidorPublico;
    }

    public void setServidorPublico(ServidorPublico servidorPublico) {
        this.servidorPublico = servidorPublico;
    }

    public String getTmpPss() {
        return tmpPss;
    }

    public void setTmpPss(String tmpPss) {
        this.tmpPss = tmpPss;
    }

    public boolean isEditar() {
        return editar;
    }

    public void setEditar(boolean editar) {
        this.editar = editar;
    }

}
