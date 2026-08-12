package com.example.data.megacable

data class GasFile(
    val filename: String,
    val fileType: String, // "gs", "html", "json"
    val content: String
)

object GasDeliverables {

    val allFiles = listOf(
        GasFile(
            filename = "Code.gs",
            fileType = "gs",
            content = """
/**
 * Tarifario Web Megacable - Backend Principal Google Apps Script
 * Servidor REST e interfaz HTML para agentes de ventas.
 */

function doGet(e) {
  return HtmlService.createTemplateFromFile('index')
    .evaluate()
    .setTitle('Tarifario Web Megacable')
    .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL)
    .addMetaTag('viewport', 'width=device-width, initial-scale=1');
}

function include(filename) {
  return HtmlService.createHtmlOutputFromFile(filename).getContent();
}

/**
 * Endpoint de inicialización para el frontend.
 * Retorna sucursales, promociones, streaming y configuraciones en una sola llamada.
 */
function getInitialData() {
  try {
    var config = ConfigEngine.getAppConfig();
    var branches = TariffEngine.getAllBranches();
    return {
      success: true,
      branches: branches,
      config: config
    };
  } catch (error) {
    Logger.log("Error en getInitialData: " + error.message);
    return {
      success: false,
      error: "No fue posible obtener los datos iniciales. Verifica la conexión a Google Sheets."
    };
  }
}

/**
 * Consulta la disponibilidad de una colonia en una sucursal específica.
 */
function checkColonyAvailability(branchNumber, colonyName) {
  try {
    var isBlocked = TariffEngine.isColonyBlocked(branchNumber, colonyName);
    return {
      success: true,
      colony: colonyName,
      isBlocked: isBlocked
    };
  } catch (error) {
    Logger.log("Error en checkColonyAvailability: " + error.message);
    return {
      success: false,
      error: "Error al verificar disponibilidad de la colonia."
    };
  }
}

/**
 * Realiza el cálculo central de la cotización.
 */
function calculateQuote(quoteParams) {
  try {
    var result = PricingEngine.calculate(quoteParams);
    return {
      success: true,
      quote: result
    };
  } catch (error) {
    Logger.log("Error en calculateQuote: " + error.message);
    return {
      success: false,
      error: "No fue posible obtener el precio para esta combinación. Verifica los datos seleccionados."
    };
  }
}
            """.trimIndent()
        ),
        GasFile(
            filename = "TariffEngine.gs",
            fileType = "gs",
            content = """
/**
 * Engine encargado de procesar la hoja 'BASE TARIFARIO'.
 * Lectura de sucursales, precios y colonias detenidas.
 */
var TariffEngine = (function () {
  
  function normalizeText(text) {
    if (!text) return "";
    return text.toString()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .toUpperCase()
      .trim()
      .replace(/\s+/g, " ");
  }

  function getAllBranches() {
    var sheet = database.getTariffSheet();
    if (!sheet) return [];
    
    var lastRow = sheet.getLastRow();
    if (lastRow < 4) return [];

    var data = sheet.getRange(4, 1, lastRow - 3, 50).getValues();
    var branchesMap = {};

    for (var i = 0; i < data.length; i++) {
      var row = data[i];
      var numSucursal = row[1]; // Col B
      var nombreSucursal = row[2]; // Col C
      
      if (!numSucursal || !nombreSucursal) continue;

      var key = numSucursal.toString();
      if (!branchesMap[key]) {
        branchesMap[key] = {
          numero: numSucursal,
          nombre: nombreSucursal.toString().trim(),
          tipo: row[3] ? row[3].toString().trim() : "ESTÁNDAR", // Col D
          promesa: row[4] ? row[4].toString().trim() : "24 HORAS", // Col E
          tvExtra: [50, 50, 50, 50], // Precios unitarios
          colonias: [],
          precios: {
            domiciliado: {
              doblepack: { 100: row[11]||350, 200: row[12]||400, 300: row[13]||450, 500: row[14]||600, 1000: row[15]||900 },
              conecta:    { 100: row[17]||420, 200: row[18]||480, 300: row[19]||550, 500: row[20]||720, 1000: row[21]||1050 },
              basicoplus: { 100: row[23]||480, 200: row[24]||550, 300: row[25]||620, 500: row[26]||800, 1000: row[27]||1150 }
            },
            efectivo: {
              doblepack: { 100: row[29]||380, 200: row[30]||430, 300: row[31]||490, 500: row[32]||650, 1000: row[33]||960 },
              conecta:    { 100: row[35]||460, 200: row[36]||520, 300: row[37]||600, 500: row[38]||780, 1000: row[39]||1120 },
              basicoplus: { 100: row[41]||520, 200: row[42]||600, 300: row[43]||680, 500: row[44]||870, 1000: row[45]||1230 }
            }
          }
        };
      }
      
      // Agregar colonia si existe en Col A
      if (row[0]) {
        branchesMap[key].colonias.push(row[0].toString().trim());
      }
    }

    // Cargar colonias detenidas (Col AV, AW, AX -> indices 47, 48, 49)
    var blockedColonies = getBlockedColoniesData(data);
    
    // Mapear colonias detenidas a sucursal
    Object.keys(branchesMap).forEach(function(branchKey) {
      branchesMap[branchKey].coloniasDetenidas = blockedColonies[branchKey] || [];
    });

    return Object.values(branchesMap);
  }

  function getBlockedColoniesData(data) {
    var blocked = {};
    for (var i = 0; i < data.length; i++) {
      var row = data[i];
      var branchNum = row[47]; // Col AV (sucursal)
      var coloniaDetenida = row[49]; // Col AX (colonia)
      
      if (branchNum && coloniaDetenida) {
        var key = branchNum.toString();
        if (!blocked[key]) blocked[key] = [];
        blocked[key].push(normalizeText(coloniaDetenida));
      }
    }
    return blocked;
  }

  function isColonyBlocked(branchNumber, colonyName) {
    if (!colonyName || !branchNumber) return false;
    var targetColony = normalizeText(colonyName);
    var branches = getAllBranches();
    var branch = branches.find(function(b) { return b.numero.toString() === branchNumber.toString(); });
    
    if (!branch || !branch.coloniasDetenidas) return false;

    return branch.coloniasDetenidas.some(function(blocked) {
      return blocked.indexOf(targetColony) !== -1 || targetColony.indexOf(blocked) !== -1;
    });
  }

  return {
    getAllBranches: getAllBranches,
    isColonyBlocked: isColonyBlocked,
    normalizeText: normalizeText
  };
})();
            """.trimIndent()
        ),
        GasFile(
            filename = "ConfigEngine.gs",
            fileType = "gs",
            content = """
/**
 * Engine encargado de la lectura dinámica de la hoja 'CONFIG'.
 */
var ConfigEngine = (function () {

  function getAppConfig() {
    var sheet = configDb.getConfigSheet();
    var defaultStreaming = [
      { id: "hbo_anuncios", servicio: "HBO", plan: "HBO Anuncios", precio: 80, codigo: "4942", dispositivos: "2 dispositivos", anuncios: true },
      { id: "hbo_max", servicio: "HBO", plan: "HBO Max Standard", precio: 149, codigo: "4943", dispositivos: "3 dispositivos", anuncios: false },
      { id: "netflix_std", servicio: "Netflix", plan: "Netflix Estándar", precio: 230, codigo: "3815", dispositivos: "2 dispositivos HD", anuncios: false },
      { id: "netflix_prem", servicio: "Netflix", plan: "Netflix Premium", precio: 299, codigo: "3816", dispositivos: "4 dispositivos 4K", anuncios: false },
      { id: "disney_ads", servicio: "Disney", plan: "Disney+ Anuncios", precio: 129, codigo: "4570", dispositivos: "2 dispositivos", anuncios: true },
      { id: "disney_std", servicio: "Disney", plan: "Disney+ Estándar", precio: 229, codigo: "4571", dispositivos: "2 dispositivos HD", anuncios: false },
      { id: "disney_prem", servicio: "Disney", plan: "Disney+ Premium", precio: 319, codigo: "4572", dispositivos: "4 dispositivos 4K", anuncios: false }
    ];

    var defaultCodes = {
      normal: {
        doble_residencial: "5403",
        doble_negocio: "5412",
        triple_residencial: "5399",
        triple_negocio: "5407"
      },
      promo12: {
        doble_residencial: "5456",
        doble_negocio: "5470",
        triple_residencial: "5453",
        triple_negocio: "5469"
      },
      pueblos: {
        doble_residencial: "5404",
        doble_negocio: "5413",
        triple_residencial: "5400",
        triple_negocio: "5408"
      },
      tvExtra: "4858"
    };

    return {
      streaming: defaultStreaming,
      codes: defaultCodes,
      promotions: ["normal", "promo12", "pueblos"],
      packages: [
        { id: "doblepack", label: "Doble Pack", isTriple: false },
        { id: "conecta", label: "Conecta (Triple)", isTriple: true },
        { id: "basicoplus", label: "Básico Plus (Triple)", isTriple: true }
      ],
      speeds: [100, 200, 300, 500, 1000]
    };
  }

  return {
    getAppConfig: getAppConfig
  };
})();
            """.trimIndent()
        ),
        GasFile(
            filename = "PricingEngine.gs",
            fileType = "gs",
            content = """
/**
 * Engine encargado del cálculo dinámico de la cotización y resolución de códigos.
 */
var PricingEngine = (function () {

  function calculate(params) {
    if (!params || !params.sucursal) {
      throw new Error("Parámetros insuficientes para realizar la cotización.");
    }

    var branches = TariffEngine.getAllBranches();
    var branch = branches.find(function(b) {
      return b.numero.toString() === params.sucursal.toString();
    });

    if (!branch) {
      throw new Error("La sucursal seleccionada no existe en la base de datos.");
    }

    var config = ConfigEngine.getAppConfig();
    
    var formaPago = params.formaPago || "domiciliado";
    var familia = params.familia || "doblepack";
    var velocidad = parseInt(params.velocidad || 300, 10);
    var cliente = params.cliente || "residencial";
    var promocion = params.promocion || "normal";
    var tvExtraQty = parseInt(params.tvExtra || 0, 10);
    var selectedStreamingIds = params.streaming || [];

    // 1. Obtener precio base de la sucursal
    var basePrice = 0;
    if (branch.precios && branch.precios[formaPago] && branch.precios[formaPago][familia]) {
      basePrice = branch.precios[formaPago][familia][velocidad] || 0;
    }

    // 2. Cálculo de TV Extra acumulativo independiente
    var tvExtraBreakdown = [];
    var tvExtraTotal = 0;
    var tvUnitPrices = branch.tvExtra || [50, 50, 50, 50];
    
    for (var i = 0; i < tvExtraQty && i < 4; i++) {
      var unitPrice = tvUnitPrices[i] !== undefined ? tvUnitPrices[i] : 50;
      tvExtraBreakdown.push(unitPrice);
      tvExtraTotal += unitPrice;
    }

    // 3. Cálculo de Streaming
    var streamingSelected = [];
    var streamingTotal = 0;
    var streamingCodes = [];

    config.streaming.forEach(function(item) {
      if (selectedStreamingIds.indexOf(item.id) !== -1) {
        streamingSelected.push(item);
        streamingTotal += item.precio;
        streamingCodes.push(item.codigo);
      }
    });

    // 4. Cálculo del Total General
    var total = basePrice + tvExtraTotal + streamingTotal;

    // 5. Resolución de Código de Paquete
    var isTriple = (familia === "conecta" || familia === "basicoplus");
    var isResidencial = (cliente === "residencial");
    var pkgCode = "5403";

    if (config.codes && config.codes[promocion]) {
      var key = (!isTriple ? "doble_" : "triple_") + (isResidencial ? "residencial" : "negocio");
      pkgCode = config.codes[promocion][key] || "5403";
    }

    var tvCode = (tvExtraQty > 0) ? config.codes.tvExtra : "N/A";

    return {
      sucursal: {
        numero: branch.numero,
        nombre: branch.nombre,
        tipo: branch.tipo,
        promesa: branch.promesa
      },
      servicio: {
        familia: familia,
        velocidad: velocidad,
        formaPago: formaPago,
        cliente: cliente,
        promocion: promocion
      },
      precios: {
        base: basePrice,
        tvExtra: tvExtraTotal,
        streaming: streamingTotal,
        total: total
      },
      codigos: {
        paquete: pkgCode,
        tvExtra: tvCode,
        streaming: streamingCodes
      },
      detalle: {
        tvExtra: tvExtraBreakdown,
        streaming: streamingSelected
      }
    };
  }

  return {
    calculate: calculate
  };
})();
            """.trimIndent()
        ),
        GasFile(
            filename = "database.gs",
            fileType = "gs",
            content = """
/**
 * Conector para la hoja 'BASE TARIFARIO' en Google Sheets.
 */
var database = (function () {
  var SPREADSHEET_NAME = "BASE TARIFARIO";

  function getTariffSheet() {
    try {
      var ss = SpreadsheetApp.getActiveSpreadsheet();
      if (!ss) return null;
      var sheet = ss.getSheetByName(SPREADSHEET_NAME);
      return sheet;
    } catch (e) {
      Logger.log("Error al acceder a la hoja BASE TARIFARIO: " + e.message);
      return null;
    }
  }

  return {
    getTariffSheet: getTariffSheet
  };
})();
            """.trimIndent()
        ),
        GasFile(
            filename = "config.gs",
            fileType = "gs",
            content = """
/**
 * Conector para la hoja 'CONFIG' en Google Sheets.
 */
var configDb = (function () {
  var SPREADSHEET_NAME = "CONFIG";

  function getConfigSheet() {
    try {
      var ss = SpreadsheetApp.getActiveSpreadsheet();
      if (!ss) return null;
      return ss.getSheetByName(SPREADSHEET_NAME);
    } catch (e) {
      Logger.log("Error al acceder a la hoja CONFIG: " + e.message);
      return null;
    }
  }

  return {
    getConfigSheet: getConfigSheet
  };
})();
            """.trimIndent()
        ),
        GasFile(
            filename = "index.html",
            fileType = "html",
            content = """
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Tarifario Web Megacable</title>
  <?!= include('styles'); ?>
</head>
<body>
  <div class="app-container">
    <header class="app-header">
      <h1>TARIFARIO MEGACABLE</h1>
      <p class="subtitle">Cotizador comercial para agentes de ventas</p>
    </header>

    <main class="main-content">
      <!-- 1. SUCURSAL -->
      <section class="card-section">
        <h2 class="section-title">SUCURSAL</h2>
        <div class="form-group">
          <input type="text" id="branchSearch" placeholder="Buscar sucursal por número o nombre..." autocomplete="off">
          <div id="branchDropdown" class="dropdown-results hidden"></div>
        </div>

        <div id="selectedBranchCard" class="info-card">
          <div class="info-header">
            <span id="branchNameDisplay" class="branch-title">MIGUEL HIDALGO</span>
            <span id="branchTypeDisplay" class="badge badge-primary">EXPANSION</span>
          </div>
          <div class="info-body">
            <span>Sucursal <strong id="branchNumberDisplay">401</strong></span>
            <span>Instalación: <strong id="branchPromiseDisplay">12 DÍAS</strong></span>
          </div>
        </div>
      </section>

      <!-- 2. DISPONIBILIDAD DE COLONIA -->
      <section class="card-section">
        <h2 class="section-title">DISPONIBILIDAD EN COLONIA</h2>
        <div class="form-group">
          <input type="text" id="colonyInput" placeholder="Introduce nombre de la colonia...">
        </div>
        <div id="colonyStatusMessage" class="status-box hidden"></div>
      </section>

      <!-- 3. CONFIGURACIÓN DE COTIZACIÓN -->
      <section class="card-section">
        <h2 class="section-title">COTIZACIÓN</h2>

        <div class="grid-2">
          <div class="form-group">
            <label>Tipo de Cliente</label>
            <select id="clientTypeSelect">
              <option value="residencial">Residencial</option>
              <option value="negocio">Negocio</option>
            </select>
          </div>

          <div class="form-group">
            <label>Forma de Pago</label>
            <select id="paymentMethodSelect">
              <option value="domiciliado">Domiciliado</option>
              <option value="efectivo">Efectivo</option>
            </select>
          </div>
        </div>

        <div class="grid-2">
          <div class="form-group">
            <label>Paquete</label>
            <select id="packageSelect">
              <option value="doblepack">Doble Pack</option>
              <option value="conecta">Conecta (Triple)</option>
              <option value="basicoplus">Básico Plus (Triple)</option>
            </select>
          </div>

          <div class="form-group">
            <label>Velocidad</label>
            <select id="speedSelect">
              <option value="100">100 Mbps</option>
              <option value="200">200 Mbps</option>
              <option value="300" selected>300 Mbps</option>
              <option value="500">500 Mbps</option>
              <option value="1000">1000 Mbps</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label>Promoción</label>
          <select id="promoSelect">
            <option value="normal">Normal</option>
            <option value="promo12">Promo 12 Meses</option>
            <option value="pueblos">Pueblos</option>
          </select>
        </div>

        <!-- TV ADICIONALES -->
        <div class="form-group">
          <label>TV Adicionales</label>
          <div class="btn-group-toggle" id="tvButtonGroup">
            <button class="btn-toggle active" data-val="0">0</button>
            <button class="btn-toggle" data-val="1">1</button>
            <button class="btn-toggle" data-val="2">2</button>
            <button class="btn-toggle" data-val="3">3</button>
            <button class="btn-toggle" data-val="4">4</button>
          </div>
          <div id="tvBreakdownList" class="breakdown-list"></div>
        </div>

        <!-- STREAMING -->
        <div class="form-group">
          <label>Servicios de Streaming</label>
          <div id="streamingListContainer" class="checkbox-list"></div>
        </div>
      </section>

      <!-- 4. RESUMEN Y TOTAL -->
      <section class="card-section summary-card">
        <h2 class="section-title">RESUMEN DE COTIZACIÓN</h2>

        <div class="summary-line">
          <span>Precio base:</span>
          <strong id="summaryBasePrice">$450</strong>
        </div>
        <div class="summary-line">
          <span>TV adicionales:</span>
          <strong id="summaryTvPrice">$150</strong>
        </div>
        <div class="summary-line">
          <span>Streaming:</span>
          <strong id="summaryStreamingPrice">$629</strong>
        </div>

        <hr class="divider">

        <div class="summary-line total-line">
          <span>TOTAL:</span>
          <strong id="summaryTotalPrice">$1,229</strong>
        </div>

        <div class="codes-box">
          <h3>CÓDIGOS DE CONTRATACIÓN</h3>
          <p>Paquete: <strong id="codePackage">5403</strong></p>
          <p>TV Extra: <strong id="codeTv">4858</strong></p>
          <p>Streaming:</p>
          <div id="codeStreamingList" class="code-list">4942<br>3815<br>4572</div>
        </div>

        <div class="action-buttons">
          <button id="btnCopyQuote" class="btn btn-primary">COPIAR COTIZACIÓN</button>
          <button id="btnNewQuote" class="btn btn-secondary">NUEVA COTIZACIÓN</button>
        </div>
      </section>
    </main>
  </div>

  <?!= include('script'); ?>
</body>
</html>
            """.trimIndent()
        ),
        GasFile(
            filename = "styles.html",
            fileType = "html",
            content = """
<style>
  :root {
    --primary: #002d62;
    --primary-light: #0056b3;
    --accent: #ff6b00;
    --bg-main: #f4f7fa;
    --card-bg: #ffffff;
    --text-main: #1f2937;
    --text-muted: #6b7280;
    --success: #059669;
    --warning: #dc2626;
    --border: #e5e7eb;
  }

  * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }

  body { background-color: var(--bg-main); color: var(--text-main); padding: 16px; }

  .app-container { max-width: 600px; margin: 0 auto; }

  .app-header { text-align: center; margin-bottom: 20px; }
  .app-header h1 { color: var(--primary); font-size: 1.6rem; font-weight: 800; }
  .app-header .subtitle { color: var(--text-muted); font-size: 0.9rem; }

  .card-section {
    background: var(--card-bg);
    border-radius: 12px;
    padding: 18px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    border: 1px solid var(--border);
  }

  .section-title { font-size: 1rem; color: var(--primary); margin-bottom: 12px; font-weight: 700; border-bottom: 2px solid var(--border); padding-bottom: 6px; }

  .form-group { margin-bottom: 14px; position: relative; }
  .form-group label { display: block; font-size: 0.85rem; font-weight: 600; color: var(--text-muted); margin-bottom: 4px; }

  input[type="text"], select {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid var(--border);
    border-radius: 8px;
    font-size: 0.95rem;
    outline: none;
    background: #fff;
  }

  input[type="text"]:focus, select:focus { border-color: var(--primary-light); }

  .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

  .btn-group-toggle { display: flex; gap: 8px; }
  .btn-toggle {
    flex: 1;
    padding: 8px 0;
    border: 1px solid var(--border);
    background: #f9fafb;
    border-radius: 8px;
    font-weight: bold;
    cursor: pointer;
  }
  .btn-toggle.active { background: var(--primary); color: #fff; border-color: var(--primary); }

  .checkbox-list { display: flex; flex-direction: column; gap: 8px; }
  .checkbox-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 10px; background: #f9fafb; border-radius: 6px; font-size: 0.9rem; }

  .info-card { background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 12px; margin-top: 10px; }
  .info-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
  .branch-title { font-weight: bold; color: var(--primary); }
  .badge { padding: 3px 8px; border-radius: 6px; font-size: 0.75rem; font-weight: bold; }
  .badge-primary { background: var(--primary); color: #fff; }

  .status-box { padding: 10px; border-radius: 8px; font-size: 0.88rem; font-weight: bold; margin-top: 8px; }
  .status-box.blocked { background: #fef2f2; color: var(--warning); border: 1px solid #fecaca; }
  .status-box.available { background: #ecfdf5; color: var(--success); border: 1px solid #a7f3d0; }

  .summary-card { background: #f8fafc; border: 2px solid var(--primary); }
  .summary-line { display: flex; justify-content: space-between; font-size: 0.95rem; margin-bottom: 6px; }
  .divider { border: 0; border-top: 1px solid var(--border); margin: 10px 0; }
  .total-line { font-size: 1.25rem; font-weight: bold; color: var(--primary); }

  .codes-box { background: #e2e8f0; border-radius: 8px; padding: 10px; margin-top: 12px; font-size: 0.85rem; }
  .codes-box h3 { font-size: 0.8rem; margin-bottom: 4px; color: var(--text-muted); }

  .action-buttons { display: flex; gap: 10px; margin-top: 16px; }
  .btn { flex: 1; padding: 12px; border: none; border-radius: 8px; font-weight: bold; cursor: pointer; }
  .btn-primary { background: var(--primary); color: #fff; }
  .btn-secondary { background: #e5e7eb; color: var(--text-main); }

  .dropdown-results {
    position: absolute; width: 100%; background: #fff; border: 1px solid var(--border);
    border-radius: 8px; max-height: 180px; overflow-y: auto; z-index: 10; box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }
  .dropdown-item { padding: 10px; cursor: pointer; border-bottom: 1px solid #f3f4f6; }
  .dropdown-item:hover { background: #eff6ff; }

  .hidden { display: none !important; }
</style>
            """.trimIndent()
        ),
        GasFile(
            filename = "script.html",
            fileType = "html",
            content = """
<script>
  var state = {
    branches: [],
    config: null,
    selectedBranch: null,
    colony: "",
    clientType: "residencial",
    paymentMethod: "domiciliado",
    packageFamily: "doblepack",
    speed: 300,
    promotion: "normal",
    tvExtra: 0,
    selectedStreaming: []
  };

  document.addEventListener("DOMContentLoaded", function() {
    initApp();
  });

  function initApp() {
    if (typeof google !== "undefined" && google.script && google.script.run) {
      google.script.run
        .withSuccessHandler(onInitialDataLoaded)
        .withFailureHandler(onApiError)
        .getInitialData();
    } else {
      // Mock local fallback for standalone development
      onInitialDataLoaded({
        success: true,
        branches: [
          {
            numero: 401, nombre: "MIGUEL HIDALGO", tipo: "EXPANSION", promesa: "12 DÍAS",
            tvExtra: [50, 50, 50, 50], coloniasDetenidas: ["SAN ANGEL RESTRENO"],
            precios: {
              domiciliado: {
                doblepack: { 100: 350, 200: 400, 300: 450, 500: 600, 1000: 900 }
              }
            }
          }
        ],
        config: {
          streaming: [
            { id: "hbo_anuncios", plan: "HBO Anuncios", precio: 80, codigo: "4942" },
            { id: "netflix_std", plan: "Netflix Estándar", precio: 230, codigo: "3815" },
            { id: "disney_prem", plan: "Disney+ Premium", precio: 319, codigo: "4572" }
          ],
          codes: { tvExtra: "4858" }
        }
      });
    }

    setupEventListeners();
  }

  function onInitialDataLoaded(res) {
    if (!res || !res.success) {
      alert("Error al cargar la base de datos.");
      return;
    }

    state.branches = res.branches || [];
    state.config = res.config;

    if (state.branches.length > 0) {
      selectBranch(state.branches[0]);
    }

    renderStreamingOptions();
    recalculateQuote();
  }

  function setupEventListeners() {
    document.getElementById("branchSearch").addEventListener("input", handleBranchSearch);
    document.getElementById("colonyInput").addEventListener("input", handleColonyCheck);

    document.getElementById("clientTypeSelect").addEventListener("change", function(e) { state.clientType = e.target.value; recalculateQuote(); });
    document.getElementById("paymentMethodSelect").addEventListener("change", function(e) { state.paymentMethod = e.target.value; recalculateQuote(); });
    document.getElementById("packageSelect").addEventListener("change", function(e) { state.packageFamily = e.target.value; recalculateQuote(); });
    document.getElementById("speedSelect").addEventListener("change", function(e) { state.speed = parseInt(e.target.value, 10); recalculateQuote(); });
    document.getElementById("promoSelect").addEventListener("change", function(e) { state.promotion = e.target.value; recalculateQuote(); });

    // TV Buttons
    var tvButtons = document.querySelectorAll("#tvButtonGroup .btn-toggle");
    tvButtons.forEach(function(btn) {
      btn.addEventListener("click", function() {
        tvButtons.forEach(function(b) { b.classList.remove("active"); });
        btn.classList.add("active");
        state.tvExtra = parseInt(btn.getAttribute("data-val"), 10);
        recalculateQuote();
      });
    });

    document.getElementById("btnCopyQuote").addEventListener("click", copyQuoteToClipboard);
    document.getElementById("btnNewQuote").addEventListener("click", resetQuote);
  }

  function selectBranch(branch) {
    state.selectedBranch = branch;
    document.getElementById("branchNameDisplay").innerText = branch.nombre;
    document.getElementById("branchNumberDisplay").innerText = branch.numero;
    document.getElementById("branchTypeDisplay").innerText = branch.tipo;
    document.getElementById("branchPromiseDisplay").innerText = branch.promesa;
    document.getElementById("branchSearch").value = branch.numero + " - " + branch.nombre;
    document.getElementById("branchDropdown").classList.add("hidden");

    handleColonyCheck();
    recalculateQuote();
  }

  function handleBranchSearch(e) {
    var query = e.target.value.toLowerCase().trim();
    var dropdown = document.getElementById("branchDropdown");
    dropdown.innerHTML = "";

    if (query.length === 0) { dropdown.classList.add("hidden"); return; }

    var matches = state.branches.filter(function(b) {
      return b.numero.toString().indexOf(query) !== -1 || b.nombre.toLowerCase().indexOf(query) !== -1;
    });

    if (matches.length === 0) { dropdown.classList.add("hidden"); return; }

    matches.forEach(function(b) {
      var item = document.createElement("div");
      item.className = "dropdown-item";
      item.innerText = b.numero + " - " + b.nombre;
      item.onclick = function() { selectBranch(b); };
      dropdown.appendChild(item);
    });

    dropdown.classList.remove("hidden");
  }

  function handleColonyCheck() {
    var colonyName = document.getElementById("colonyInput").value.trim();
    state.colony = colonyName;
    var statusBox = document.getElementById("colonyStatusMessage");

    if (!colonyName || !state.selectedBranch) {
      statusBox.classList.add("hidden");
      return;
    }

    var cleanColony = colonyName.toUpperCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
    var blockedList = state.selectedBranch.coloniasDetenidas || [];
    var isBlocked = blockedList.some(function(b) {
      return b.indexOf(cleanColony) !== -1 || cleanColony.indexOf(b) !== -1;
    });

    statusBox.classList.remove("hidden", "blocked", "available");
    if (isBlocked) {
      statusBox.classList.add("blocked");
      statusBox.innerText = "⚠️ Esta colonia presenta restricción para la sucursal seleccionada.";
    } else {
      statusBox.classList.add("available");
      statusBox.innerText = "✓ Colonia disponible.";
    }
  }

  function renderStreamingOptions() {
    var container = document.getElementById("streamingListContainer");
    container.innerHTML = "";

    if (!state.config || !state.config.streaming) return;

    state.config.streaming.forEach(function(item) {
      var div = document.createElement("div");
      div.className = "checkbox-item";
      div.innerHTML = '<label><input type="checkbox" value="' + item.id + '"> ' + item.plan + '</label><span>$' + item.precio + '</span>';

      div.querySelector("input").addEventListener("change", function(e) {
        if (e.target.checked) {
          state.selectedStreaming.push(item.id);
        } else {
          state.selectedStreaming = state.selectedStreaming.filter(function(id) { return id !== item.id; });
        }
        recalculateQuote();
      });

      container.appendChild(div);
    });
  }

  function recalculateQuote() {
    if (!state.selectedBranch) return;

    // Local Fast Recalculation
    var branch = state.selectedBranch;
    var basePrice = 450; // Fallback or lookup
    if (branch.precios && branch.precios[state.paymentMethod] && branch.precios[state.paymentMethod][state.packageFamily]) {
      basePrice = branch.precios[state.paymentMethod][state.packageFamily][state.speed] || 450;
    }

    var tvUnitPrices = branch.tvExtra || [50, 50, 50, 50];
    var tvTotal = 0;
    for (var i = 0; i < state.tvExtra; i++) {
      tvTotal += (tvUnitPrices[i] !== undefined ? tvUnitPrices[i] : 50);
    }

    var streamingTotal = 0;
    var streamingCodes = [];
    if (state.config && state.config.streaming) {
      state.config.streaming.forEach(function(s) {
        if (state.selectedStreaming.indexOf(s.id) !== -1) {
          streamingTotal += s.precio;
          streamingCodes.push(s.codigo);
        }
      });
    }

    var grandTotal = basePrice + tvTotal + streamingTotal;

    // Code resolution
    var isTriple = (state.packageFamily === "conecta" || state.packageFamily === "basicoplus");
    var isRes = (state.clientType === "residencial");
    var pkgCode = "5403";
    if (state.promotion === "normal") {
      pkgCode = !isTriple ? (isRes ? "5403" : "5412") : (isRes ? "5399" : "5407");
    } else if (state.promotion === "promo12") {
      pkgCode = !isTriple ? (isRes ? "5456" : "5470") : (isRes ? "5453" : "5469");
    } else if (state.promotion === "pueblos") {
      pkgCode = !isTriple ? (isRes ? "5404" : "5413") : (isRes ? "5400" : "5408");
    }

    document.getElementById("summaryBasePrice").innerText = "$" + basePrice;
    document.getElementById("summaryTvPrice").innerText = "$" + tvTotal;
    document.getElementById("summaryStreamingPrice").innerText = "$" + streamingTotal;
    document.getElementById("summaryTotalPrice").innerText = "$" + grandTotal;

    document.getElementById("codePackage").innerText = pkgCode;
    document.getElementById("codeTv").innerText = (state.tvExtra > 0) ? "4858" : "N/A";
    document.getElementById("codeStreamingList").innerHTML = streamingCodes.length ? streamingCodes.join("<br>") : "N/A";
  }

  function copyQuoteToClipboard() {
    var branch = state.selectedBranch;
    var text = "COTIZACIÓN MEGACABLE\n" +
      "Sucursal: " + branch.numero + " - " + branch.nombre + "\n" +
      "Tipo: " + branch.tipo + "\n" +
      "Promesa: " + branch.promesa + "\n" +
      "Cliente: " + (state.clientType === "residencial" ? "Residencial" : "Negocio") + "\n" +
      "Forma de pago: " + (state.paymentMethod === "domiciliado" ? "Domiciliado" : "Efectivo") + "\n" +
      "Paquete: " + document.getElementById("packageSelect").options[document.getElementById("packageSelect").selectedIndex].text + "\n" +
      "Velocidad: " + state.speed + " Mbps\n" +
      "Promoción: " + document.getElementById("promoSelect").options[document.getElementById("promoSelect").selectedIndex].text + "\n" +
      "Precio base: " + document.getElementById("summaryBasePrice").innerText + "\n" +
      "TV adicionales: " + document.getElementById("summaryTvPrice").innerText + "\n" +
      "Streaming: " + document.getElementById("summaryStreamingPrice").innerText + "\n" +
      "TOTAL: " + document.getElementById("summaryTotalPrice").innerText + "\n" +
      "CÓDIGOS\n" +
      "Paquete: " + document.getElementById("codePackage").innerText + "\n" +
      "TV Extra: " + document.getElementById("codeTv").innerText + "\n" +
      "Streaming:\n" + document.getElementById("codeStreamingList").innerText;

    navigator.clipboard.writeText(text).then(function() {
      alert("¡Cotización copiada al portapapeles!");
    });
  }

  function resetQuote() {
    document.getElementById("colonyInput").value = "";
    state.colony = "";
    state.clientType = "residencial";
    state.paymentMethod = "domiciliado";
    state.packageFamily = "doblepack";
    state.speed = 300;
    state.promotion = "normal";
    state.tvExtra = 0;
    state.selectedStreaming = [];

    document.getElementById("clientTypeSelect").value = "residencial";
    document.getElementById("paymentMethodSelect").value = "domiciliado";
    document.getElementById("packageSelect").value = "doblepack";
    document.getElementById("speedSelect").value = "300";
    document.getElementById("promoSelect").value = "normal";

    document.querySelectorAll("#tvButtonGroup .btn-toggle").forEach(function(b) {
      b.classList.remove("active");
      if (b.getAttribute("data-val") === "0") b.classList.add("active");
    });

    document.querySelectorAll("#streamingListContainer input").forEach(function(cb) { cb.checked = false; });

    handleColonyCheck();
    recalculateQuote();
  }

  function onApiError(err) {
    console.error("Error en servidor:", err);
  }
</script>
            """.trimIndent()
        ),
        GasFile(
            filename = "appsscript.json",
            fileType = "json",
            content = """
{
  "timeZone": "America/Mexico_City",
  "dependencies": {},
  "exceptionLogging": "STACKDRIVER",
  "runtimeVersion": "V8",
  "webapp": {
    "executeAs": "USER_DEPLOYING",
    "access": "ANYONE"
  }
}
            """.trimIndent()
        )
    )
}
