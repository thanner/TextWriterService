package br.edu.ufrgs.inf.bpm.serviceregistry;

import application.ServiceName;
import br.edu.ufrgs.inf.bpm.Veiculo;
import org.springframework.stereotype.Service;

@Service
public class VeiculoService implements ServiceName {

    @Override
    public Veiculo search(String appid) {
        Veiculo veiculo = new Veiculo();
        veiculo.setCor("azul");
        veiculo.setName(appid);
        veiculo.setNumeroRodas(3);

        return veiculo;
    }

    @Override
    public void insert(String appid, int results) {

    }

}
