/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.pcvolkmer.onco.datamapper.fhir;

import dev.pcvolkmer.mv64e.model.MtbCarePlan;
import dev.pcvolkmer.mv64e.model.PatientRecord;
import dev.pcvolkmer.onco.datamapper.fhir.biomarker.BrcanessMapper;
import dev.pcvolkmer.onco.datamapper.fhir.biomarker.HrdScoreMapper;
import dev.pcvolkmer.onco.datamapper.fhir.biomarker.MsiMapper;
import dev.pcvolkmer.onco.datamapper.fhir.biomarker.TmbMapper;
import dev.pcvolkmer.onco.datamapper.fhir.careplan.HumangenetischeBeratungMapper;
import dev.pcvolkmer.onco.datamapper.fhir.careplan.StudieneinschlussMapper;
import dev.pcvolkmer.onco.datamapper.fhir.careplan.TherapieempfehlungMapper;
import dev.pcvolkmer.onco.datamapper.fhir.careplan.TherapieplanMapper;
import dev.pcvolkmer.onco.datamapper.fhir.diagnosis.*;
import dev.pcvolkmer.onco.datamapper.fhir.ngs.*;
import dev.pcvolkmer.onco.datamapper.fhir.pathology.IhcMapper;
import dev.pcvolkmer.onco.datamapper.fhir.pathology.MolekularPathologieBefundMapper;
import dev.pcvolkmer.onco.datamapper.fhir.tnm.TnmMMapper;
import dev.pcvolkmer.onco.datamapper.fhir.tnm.TnmNMapper;
import dev.pcvolkmer.onco.datamapper.fhir.tnm.TnmTMapper;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientRecordMapper {

  private static final Logger LOG = LoggerFactory.getLogger(PatientRecordMapper.class);

  private final Bundle.BundleType bundleType;

  private PatientRecordMapper(Bundle.BundleType bundleType) {
    this.bundleType = bundleType;
  }

  public static PatientRecordMapper defaultInstance() {
    return new PatientRecordMapper(Bundle.BundleType.TRANSACTION);
  }

  public Bundle mapToBundle(PatientRecord patientRecord) {
    final var bundle = new Bundle();

    bundle.setType(this.bundleType);

    final var diagnoses = patientRecord.getDiagnoses();
    if (null != diagnoses) {
      final var mtbDiagnoseMapper = new MtbDiagnoseMapper();
      diagnoses.forEach(item -> mtbDiagnoseMapper.addToBundle(bundle, item));

      final var oncoDiagnoseMapper = new OncoDiagnoseMapper();
      diagnoses.forEach(item -> oncoDiagnoseMapper.addToBundle(bundle, item));

      final var tumorausbreitungMapper = new TumorausbreitungMapper();
      diagnoses.forEach(item -> tumorausbreitungMapper.addManyToBundle(bundle, item));

      final var tnmtMapper = new TnmTMapper();
      diagnoses.forEach(item -> tnmtMapper.addManyToBundle(bundle, item));

      final var tnmnMapper = new TnmNMapper();
      diagnoses.forEach(item -> tnmnMapper.addManyToBundle(bundle, item));

      final var tnmmMapper = new TnmMMapper();
      diagnoses.forEach(item -> tnmmMapper.addManyToBundle(bundle, item));
    }

    final var carePlans = patientRecord.getCarePlans();
    if (null != carePlans) {
      final var therapieplanMapper = new TherapieplanMapper();
      carePlans.forEach(item -> therapieplanMapper.addToBundle(bundle, item));

      final var humangenetischeBeratungMapper = new HumangenetischeBeratungMapper();
      carePlans.stream()
          .map(MtbCarePlan::getGeneticCounselingRecommendation)
          .filter(Objects::nonNull)
          .forEach(item -> humangenetischeBeratungMapper.addToBundle(bundle, item));

      final var therapieempfehlungMapper = new TherapieempfehlungMapper();
      carePlans.stream()
          .filter(item -> item.getMedicationRecommendations() != null)
          .flatMap(item -> item.getMedicationRecommendations().stream())
          .forEach(item -> therapieempfehlungMapper.addToBundle(bundle, item));

      final var studieneinschlussMapper = new StudieneinschlussMapper();
      carePlans.stream()
          .filter(item -> item.getStudyEnrollmentRecommendations() != null)
          .flatMap(item -> item.getStudyEnrollmentRecommendations().stream())
          .forEach(item -> studieneinschlussMapper.addToBundle(bundle, item));
    }

    final var histologieReports = patientRecord.getHistologyReports();
    if (null != histologieReports) {
      final var tumorzellgehaltMapper = new TumorzellgehaltMapper();
      histologieReports.forEach(item -> tumorzellgehaltMapper.addToBundle(bundle, item));
    }

    if (null != histologieReports && null != diagnoses) {
      final var oncotreeMapper = new OncotreeMapper();
      oncotreeMapper.addManyToBundle(bundle, patientRecord);
    }

    final var performanceStatus = patientRecord.getPerformanceStatus();
    if (null != performanceStatus) {
      final var ecogMapper = new EcogMapper();
      performanceStatus.forEach(item -> ecogMapper.addToBundle(bundle, item));
    }

    final var ngsReports = patientRecord.getNgsReports();
    if (null != ngsReports) {

      final var einfacheVarianteMapper = new EinfacheVarianteMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getSimpleVariants() != null)
          .flatMap(item -> item.getResults().getSimpleVariants().stream())
          .forEach(item -> einfacheVarianteMapper.addToBundle(bundle, item));

      final var diagnostischeImplikationMapper =
          new DiagnostischeImplikationMapper(einfacheVarianteMapper);
      ngsReports.stream()
          .filter(item -> item.getResults().getSimpleVariants() != null)
          .flatMap(item -> item.getResults().getSimpleVariants().stream())
          .filter(diagnostischeImplikationMapper::supports)
          .forEach(item -> diagnostischeImplikationMapper.addToBundle(bundle, item));

      final var cnvMapper = new CnvMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getCopyNumberVariants() != null)
          .flatMap(item -> item.getResults().getCopyNumberVariants().stream())
          .forEach(item -> cnvMapper.addToBundle(bundle, item));

      final var dnaFusionMapper = new DnaFusionMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getDnaFusions() != null)
          .flatMap(item -> item.getResults().getDnaFusions().stream())
          .forEach(item -> dnaFusionMapper.addToBundle(bundle, item));

      final var rnaFusionMapper = new RnaFusionMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getRnaFusions() != null)
          .flatMap(item -> item.getResults().getRnaFusions().stream())
          .forEach(item -> rnaFusionMapper.addToBundle(bundle, item));

      final var hrdScoreMapper = new HrdScoreMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getHrdScore() != null)
          .map(item -> item.getResults().getHrdScore())
          .forEach(item -> hrdScoreMapper.addToBundle(bundle, item));

      final var brcanessMapper = new BrcanessMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getBrcaness() != null)
          .map(item -> item.getResults().getBrcaness())
          .forEach(item -> brcanessMapper.addToBundle(bundle, item));

      final var tmbMapper = new TmbMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getTmb() != null)
          .map(item -> item.getResults().getTmb())
          .forEach(item -> tmbMapper.addToBundle(bundle, item));
    }

    final var msiFindings = patientRecord.getMsiFindings();
    if (null != msiFindings) {
      final var msiMapper = new MsiMapper();
      msiFindings.forEach(
          item -> {
            try {
              msiMapper.addToBundle(bundle, item);
            } catch (IllegalArgumentException e) {
              LOG.warn(
                  "MSI interpretation with MMR is not supported and ignored in msiFindings[{}]",
                  item.getId());
            }
          });
    }

    final var ihcReports = patientRecord.getIhcReports();
    if (ihcReports != null) {
      final var ihcMapper = new IhcMapper();
      final var molekularPathologieBefundMapper = new MolekularPathologieBefundMapper(ihcMapper);

      ihcReports.forEach(
          item -> {
            ihcMapper.addManyToBundle(bundle, item);
            molekularPathologieBefundMapper.addToBundle(bundle, item);
          });
    }

    return bundle;
  }
}
