package br.com.crowe.Contratos;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class CriarNovoContrato implements AcaoRotinaJava {

	private static final SimpleDateFormat ddMMyyy = new SimpleDateFormat("dd/MM/yyyy");
	BigDecimal numContrato;
	BigDecimal codParc;
	BigDecimal codCencus;
	BigDecimal codNat;
	BigDecimal newCodProj;
	BigDecimal codEmp;
	BigDecimal codUsu;
	BigDecimal codTipRdv;
	BigDecimal codNatDes;
	BigDecimal codTipOper;
	BigDecimal codEmpDes;
	BigDecimal totVenda;

	String ambiente;
	String gerarNf;
	String tipoContrato;
	String tipoTitulo;
	String ativo;
	String descrCencus;
	String nomeParc;
	String msg;
	String reembolso;

	Timestamp dtContrato;

	@Override
	public void doAction(ContextoAcao contexto) throws Exception {
		// TODO Auto-generated method stub

		JdbcWrapper JDBC = JapeFactory.getEntityFacade().getJdbcWrapper();
		NativeSql nativeSql = new NativeSql(JDBC);
		SessionHandle hnd = JapeSession.open();

		for (int i = 0; i < (contexto.getLinhas()).length; i++) {

			if ((contexto.getLinhas()).length == 0) {
				contexto.mostraErro("Selecione um registro antes.");
			} else if ((contexto.getLinhas()).length > 1) {
				contexto.mostraErro("Selecione apenas um registro.");
			}

			Registro linha = contexto.getLinhas()[i];
			numContrato = (BigDecimal) linha.getCampo("NUMCONTRATO");

			ResultSet query = nativeSql.executeQuery(
					"SELECT "
					+ " CODPARC, "
					+ " AMBIENTE, "
					+ " CODCENCUS, "
					+ " CODNAT, "
					+ " DTCONTRATO, "
					+ " NUMCONTRATO, "
					+ " GERARNF, "
					+ " TIPOCONTRATO, "
					+ " TIPOTITULO, "
					+ " CODCENCUS, "
					+ " ATIVO,"
					+ " CODEMP,"
					+ " AD_TOTVENDA"
					+ " FROM TCSCON WHERE NUMCONTRATO = " + numContrato);

			while (query.next()) {
				codParc = query.getBigDecimal("CODPARC");
				ambiente = query.getString("AMBIENTE");
				codCencus = query.getBigDecimal("CODCENCUS");
				codNat = query.getBigDecimal("CODNAT");
				dtContrato = query.getTimestamp("DTCONTRATO");
				numContrato = query.getBigDecimal("NUMCONTRATO");
				gerarNf = query.getString("GERARNF");
				tipoContrato = query.getString("TIPOCONTRATO");
				tipoTitulo = query.getString("TIPOTITULO");
				ativo = query.getString("ATIVO");
				codEmp = query.getBigDecimal("CODEMP");
				totVenda = query.getBigDecimal("AD_TOTVENDA");
			}

			ResultSet queryCus = nativeSql
					.executeQuery("SELECT " + "DESCRCENCUS " + "FROM TSICUS WHERE CODCENCUS = " + codCencus);

			while (queryCus.next()) {
				descrCencus = queryCus.getString("DESCRCENCUS");
				System.out.println("descrCencus" + descrCencus);
			}

			ResultSet queryPar = nativeSql
					.executeQuery("\r\n" + "SELECT NOMEPARC FROM TGFPAR WHERE CODPARC = " + codParc);

			while (queryPar.next()) {
				nomeParc = queryPar.getString("NOMEPARC");
				nomeParc = nomeParc.substring(0, 30);
				System.out.println("nomeParc" + nomeParc);
			}

			String texto = (nomeParc.trim().replaceAll("\\s+", " ")) + "-" + descrCencus.trim().replaceAll("\\s+", " ")
					+ "-" + ambiente.trim().replaceAll("\\s+", " ");

			System.out.println("texto formatado : " + texto);

			ResultSet rs1 = nativeSql.executeQuery("SELECT MAX(CODPROJ) +1 AS CODPROJ FROM TCSPRJ ");

			if (rs1.next()) {
				newCodProj = rs1.getBigDecimal("CODPROJ");
				System.out.println("codproj capturado : " + newCodProj);

				try {

					System.out.println("Inserindo na tabela projeto");

					EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
					DynamicVO dynamicVO1 = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("Projeto");

					dynamicVO1.setProperty("CODPROJ", newCodProj);
					System.out.println(newCodProj);
					// dynamicVO1.setProperty("CODPROJPAI", codProjPai);
					dynamicVO1.setProperty("CODEMP", codEmp);
					dynamicVO1.setProperty("AD_CODPARC", codParc);
					dynamicVO1.setProperty("AD_CODNAT", new BigDecimal(40101));
					 dynamicVO1.setProperty("AD_CODTIPOPER", new BigDecimal(132));
					dynamicVO1.setProperty("GRAU", new BigDecimal(1));
					dynamicVO1.setProperty("ABREVIATURA", ambiente);
					System.out.println("ambiemte" + ambiente);
					dynamicVO1.setProperty("IDENTIFICACAO", texto);
					dynamicVO1.setProperty("ATIVO", "S");
					dynamicVO1.setProperty("ANALITICO", "S");
					dynamicVO1.setProperty("AD_VLRPROJETO", totVenda );
					// dynamicVO1.setProperty("CODPROD", codProd);
					PersistentLocalEntity createEntity = dwfFacade.createEntity("Projeto", (EntityVO) dynamicVO1);
					DynamicVO save = (DynamicVO) createEntity.getValueObject();

					// newCodProj = save.asBigDecimal("CODPROJ");
					System.out.println("CODPROJ " + newCodProj);

				} catch (Exception e) {
					msg = "Erro na inclusao dos Itens " + e.getMessage();
					System.out.println(msg);
				}
			}

			ResultSet aprovadores = nativeSql
					.executeQuery("SELECT APR.CODEMP, EMP.RAZAOSOCIAL,EMP.RAZAOABREV, USU.CODUSU, USU.NOMEUSU"
							+ " FROM AD_APROVEMP APR" + " INNER JOIN TSIEMP EMP ON (EMP.CODEMP = APR.CODEMP)"
							+ " INNER JOIN TSIUSU USU ON (USU.CODUSU = APR.CODUSU)" + " WHERE APR.CODEMP = " + codEmp
							+ " ORDER BY APR.CODEMP");

			while (aprovadores.next()) {

				codUsu = aprovadores.getBigDecimal("CODUSU");

				try {

					EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
					DynamicVO dynamicVO1 = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("AD_APRVSPROJETO");
					dynamicVO1.setProperty("CODPROJ", newCodProj);
					dynamicVO1.setProperty("CODUSU", codUsu);
					PersistentLocalEntity createEntity = dwfFacade.createEntity("AD_APRVSPROJETO",
							(EntityVO) dynamicVO1);
					DynamicVO save = (DynamicVO) createEntity.getValueObject();

				} catch (Exception e) {
					msg = "Erro na inclusao dos Itens " + e.getMessage();
					System.out.println(msg);
				}
			}

			ResultSet despesas = nativeSql.executeQuery("SELECT"
					+ " REG.CODEMP, EMP.RAZAOSOCIAL, EMP.RAZAOABREV , REG.SEQ , REG.CODTIPRDV, TIP.NOMETIPRDV, REG.EREEMBOLSAVEL, "
					+ " REG.CODNAT , REG.CODTIPOPER, REG.VLRLIMITE, REG.CLIREE, REG.OBGANEXO, REG.ATIVO "
					+ " FROM AD_REGRAPROJETOB REG" + " INNER JOIN TSIEMP EMP ON (EMP.CODEMP = REG.CODEMP)"
					+ " INNER JOIN AD_TIPORDV TIP ON (TIP.CODTIPRDV = REG.CODTIPRDV)" + " WHERE REG.CODEMP =" + codEmp
					+ " ORDER BY REG.CODEMP");

			while (despesas.next()) {

				codTipRdv = despesas.getBigDecimal("CODTIPRDV");
				reembolso = despesas.getString("EREEMBOLSAVEL");
				codNatDes = despesas.getBigDecimal("CODNAT");
				codTipOper = despesas.getBigDecimal("CODTIPOPER");
				codEmpDes = despesas.getBigDecimal("CODEMP");

				try {

					EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
					DynamicVO dynamicVO1 = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("AD_REGRAPROJETO");

					dynamicVO1.setProperty("CODPROJ", newCodProj);
					dynamicVO1.setProperty("CODTIPRDV", codTipRdv);
					dynamicVO1.setProperty("EREEMBOLSAVEL", reembolso);
					dynamicVO1.setProperty("CODNAT", codNatDes);
					dynamicVO1.setProperty("CODTIPOPER", codTipOper);
					dynamicVO1.setProperty("CODEMP", codEmpDes);

					PersistentLocalEntity createEntity = dwfFacade.createEntity("AD_REGRAPROJETO",
							(EntityVO) dynamicVO1);
					DynamicVO save = (DynamicVO) createEntity.getValueObject();

				} catch (Exception e) {
					msg = "Erro na inclusao dos Itens " + e.getMessage();
					System.out.println(msg);
				}
			}
			
			boolean update = nativeSql
					.executeUpdate(" UPDATE TCSCON SET AD_PROJETO = 'SIM' WHERE NUMCONTRATO = " + numContrato);
			System.out.println(update);
			
			contexto.setMensagemRetorno("Projeto Criado : " +newCodProj);
		}
	}
}
