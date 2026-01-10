import React, { useMemo, useState } from "react";

export default function App() {
  const [texto, setTexto] = useState("");
  const [file, setFile] = useState(null);
  const [resultado, setResultado] = useState("");
  const [loading, setLoading] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [error, setError] = useState("");

  // Si tienes proxy en package.json, pon baseURL = "" (vacío)
  // Si NO tienes proxy, deja esto:
  const baseURL = useMemo(() => "http://localhost:8080", []);

  const buildFormData = () => {
    const fd = new FormData();
    if (texto && texto.trim()) fd.append("texto", texto.trim());
    if (file) fd.append("file", file);
    return fd;
  };

  const limpiar = () => {
    setTexto("");
    setFile(null);
    setResultado("");
    setError("");
  };

  // ✅ Mostrar dictamen en pantalla (JSON)
  const analizarYMostrar = async () => {
    setError("");
    setLoading(true);

    try {
      const resp = await fetch(`${baseURL}/api/balanza/analizar-texto`, {
        method: "POST",
        body: buildFormData(),
      });

      const contentType = resp.headers.get("content-type") || "";

      if (!resp.ok) {
        // intenta leer detalle del backend si viene en JSON
        let msg = `HTTP ${resp.status}`;
        if (contentType.includes("application/json")) {
          const j = await resp.json();
          msg = j?.message || j?.error || msg;
        } else {
          const t = await resp.text();
          if (t) msg = t;
        }
        throw new Error(msg);
      }

      const data = await resp.json();
      const dictamen = data?.dictamen ?? "";
      setResultado(dictamen || "(Sin dictamen)");
    } catch (e) {
      setError(e?.message || "Error al conectar con el servidor.");
    } finally {
      setLoading(false);
    }
  };

  // ✅ Descargar PDF (BLOB)
  const descargarPDF = async () => {
    setError("");
    setDownloading(true);

    try {
      const resp = await fetch(`${baseURL}/api/balanza/analizar`, {
        method: "POST",
        body: buildFormData(),
      });

      if (!resp.ok) {
        let msg = `HTTP ${resp.status}`;
        try {
          const t = await resp.text();
          if (t) msg = t;
        } catch {}
        throw new Error(msg);
      }

      const blob = await resp.blob();

      // Nombre sugerido desde backend (Content-Disposition) si existe
      const cd = resp.headers.get("content-disposition") || "";
      let filename = "dictamen_balanza.pdf";
      const match = cd.match(/filename="?([^"]+)"?/i);
      if (match?.[1]) filename = match[1];

      const url = window.URL.createObjectURL(blob);

      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      a.remove();

      window.URL.revokeObjectURL(url);
    } catch (e) {
      setError(e?.message || "Failed to fetch");
    } finally {
      setDownloading(false);
    }
  };

  // ---------- estilos simples (mejor estética) ----------
  const styles = {
    page: {
      minHeight: "100vh",
      background: "#f6f8fb",
      padding: "28px 16px",
      fontFamily:
        'system-ui, -apple-system, Segoe UI, Roboto, "Helvetica Neue", Arial',
      color: "#0f172a",
    },
    shell: {
      maxWidth: 1180,
      margin: "0 auto",
      background: "#fff",
      borderRadius: 16,
      boxShadow: "0 10px 30px rgba(15, 23, 42, 0.08)",
      padding: 22,
    },
    header: { display: "flex", alignItems: "center", gap: 12, marginBottom: 14 },
    badge: {
      width: 44,
      height: 44,
      borderRadius: 12,
      background: "#eef2ff",
      display: "grid",
      placeItems: "center",
      fontSize: 22,
    },
    title: { margin: 0, fontSize: 28, fontWeight: 800 },
    subtitle: { margin: "6px 0 0 0", color: "#475569" },
    grid: { display: "grid", gridTemplateColumns: "1.05fr 0.95fr", gap: 16 },
    card: {
      border: "1px solid #e2e8f0",
      borderRadius: 14,
      padding: 16,
      background: "#fff",
    },
    h3: { margin: "0 0 10px 0", fontSize: 16, fontWeight: 800 },
    textarea: {
      width: "100%",
      minHeight: 240,
      borderRadius: 12,
      border: "1px solid #cbd5e1",
      padding: 12,
      fontSize: 14,
      outline: "none",
      resize: "vertical",
    },
    row: { display: "flex", alignItems: "center", gap: 10, flexWrap: "wrap" },
    file: {
      padding: "8px 10px",
      borderRadius: 12,
      border: "1px dashed #cbd5e1",
      background: "#f8fafc",
      width: "100%",
    },
    btnPrimary: {
      background: "#2563eb",
      color: "#fff",
      border: "none",
      borderRadius: 12,
      padding: "10px 14px",
      fontWeight: 700,
      cursor: "pointer",
    },
    btnSoft: {
      background: "#eff6ff",
      color: "#1d4ed8",
      border: "1px solid #dbeafe",
      borderRadius: 12,
      padding: "10px 14px",
      fontWeight: 700,
      cursor: "pointer",
    },
    btnGhost: {
      background: "#fff",
      color: "#334155",
      border: "1px solid #cbd5e1",
      borderRadius: 12,
      padding: "10px 14px",
      fontWeight: 700,
      cursor: "pointer",
    },
    alert: {
      marginTop: 12,
      padding: "10px 12px",
      borderRadius: 12,
      border: "1px solid #fecaca",
      background: "#fff1f2",
      color: "#991b1b",
      fontWeight: 700,
    },
    resultBox: {
      width: "100%",
      minHeight: 430,
      borderRadius: 12,
      border: "1px solid #cbd5e1",
      padding: 12,
      background: "#fbfdff",
      overflow: "auto",
      whiteSpace: "pre-wrap",
      lineHeight: 1.4,
      fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, "Courier New"',
      fontSize: 13,
    },
    tip: { marginTop: 10, color: "#64748b", fontSize: 13 },
    footer: { marginTop: 16, textAlign: "center", color: "#94a3b8" },
  };

  return (
    <div style={styles.page}>
      <div style={styles.shell}>
        <div style={styles.header}>
          <div style={styles.badge}>⚖️</div>
          <div>
            <h1 style={styles.title}>Balanza Legal IA</h1>
            <p style={styles.subtitle}>
              Analiza un relato o documento y genera un dictamen técnico-legal.
            </p>
          </div>
        </div>

        <div style={styles.grid}>
          {/* Izquierda */}
          <div style={styles.card}>
            <h3 style={styles.h3}>Opción 1: Escribir Relato de Hechos</h3>
            <textarea
              style={styles.textarea}
              value={texto}
              onChange={(e) => setTexto(e.target.value)}
              placeholder="Escribe los detalles del caso (hechos, fechas, partes, pruebas, qué deseas solicitar)..."
            />

            <div style={{ height: 14 }} />

            <h3 style={styles.h3}>Opción 2: Subir archivo (PDF o Word)</h3>
            <div style={styles.file}>
              <input
                type="file"
                accept=".pdf,.doc,.docx"
                onChange={(e) => setFile(e.target.files?.[0] || null)}
              />
              {file && (
                <div style={{ marginTop: 8, color: "#334155", fontWeight: 700 }}>
                  Archivo: {file.name}
                </div>
              )}
            </div>

            <div style={{ height: 14 }} />

            <div style={styles.row}>
              <button
                style={styles.btnPrimary}
                onClick={analizarYMostrar}
                disabled={loading}
              >
                {loading ? "Analizando..." : "Analizar y mostrar"}
              </button>

              <button
                style={styles.btnSoft}
                onClick={descargarPDF}
                disabled={downloading}
              >
                {downloading ? "Generando PDF..." : "Descargar PDF"}
              </button>

              <button style={styles.btnGhost} onClick={limpiar}>
                Limpiar
              </button>
            </div>

            {error && <div style={styles.alert}>⚠️ {error}</div>}

            <div style={styles.tip}>
              Tip: si Gemini te da 429 (cuota), el backend puede devolver un
              dictamen de contingencia o pedir reintentar.
            </div>
          </div>

          {/* Derecha */}
          <div style={styles.card}>
            <h3 style={styles.h3}>Resultado en pantalla</h3>
            <div style={styles.resultBox}>
              {resultado
                ? resultado
                : 'Aquí se mostrará el dictamen cuando presiones "Analizar y mostrar".'}
            </div>
          </div>
        </div>

        <div style={styles.footer}>© 2026 Balanza Legal IA</div>
      </div>
    </div>
  );
}
