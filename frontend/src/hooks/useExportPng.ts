import { useCallback, useState } from 'react';
import { toBlob } from 'html-to-image';

export type ExportStatus = 'idle' | 'exporting' | 'error';

/** Attribut à poser sur les éléments à exclure de l'image (boutons, contrôles…). */
export const EXPORT_IGNORE_ATTRIBUTE = 'data-export-ignore';

function isIgnored(node: Node): boolean {
  return node instanceof Element && node.hasAttribute(EXPORT_IGNORE_ATTRIBUTE);
}

function download(blob: Blob, fileName: string): void {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.rel = 'noopener';
  document.body.appendChild(link);
  link.click();
  link.remove();
  // Laisse le temps au navigateur de démarrer le téléchargement avant de libérer l'URL.
  setTimeout(() => URL.revokeObjectURL(url), 1_000);
}

async function shareOrDownload(blob: Blob, fileName: string, title: string): Promise<void> {
  const file = new File([blob], fileName, { type: 'image/png' });
  if (typeof navigator.canShare === 'function' && navigator.canShare({ files: [file] })) {
    try {
      await navigator.share({ files: [file], title });
      return;
    } catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        return; // partage annulé par l'utilisateur
      }
      // Partage refusé (ex. activation utilisateur expirée) : repli sur le téléchargement.
    }
  }
  download(blob, fileName);
}

/**
 * Exporte un nœud DOM en PNG (html-to-image), puis le partage (Web Share API) ou le télécharge.
 */
export function useExportPng(): {
  status: ExportStatus;
  exportNode: (node: HTMLElement, fileName: string, title: string) => Promise<void>;
} {
  const [status, setStatus] = useState<ExportStatus>('idle');

  const exportNode = useCallback(async (node: HTMLElement, fileName: string, title: string) => {
    setStatus('exporting');
    try {
      await document.fonts.ready;
      const blob = await toBlob(node, {
        pixelRatio: 2,
        cacheBust: true,
        filter: (domNode) => !isIgnored(domNode),
      });
      if (blob === null) {
        throw new Error('Export PNG vide');
      }
      await shareOrDownload(blob, fileName, title);
      setStatus('idle');
    } catch {
      setStatus('error');
    }
  }, []);

  return { status, exportNode };
}
