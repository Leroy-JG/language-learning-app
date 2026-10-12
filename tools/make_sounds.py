"""Génère les bruitages de l'app (res/raw/sfx_*.wav) : sons de synthèse, sans licence tierce.

    python3 tools/make_sounds.py app/src/main/res/raw
"""
import sys
import wave
from pathlib import Path

import numpy as np

RATE = 44100


def note(freq, dur, start=0.0, total=None, decay=6.0, partials=((1, 1.0), (2, 0.25), (3, 0.08)), bell=0.0):
    """Une note : sinusoïdes harmoniques, attaque douce, décroissance exponentielle."""
    total = total or (start + dur)
    t = np.arange(int(RATE * dur)) / RATE
    wave_ = sum(a * np.sin(2 * np.pi * freq * k * t) for k, a in partials)
    if bell:
        wave_ += bell * np.sin(2 * np.pi * freq * 2.76 * t) * np.exp(-t * decay * 2.5)
    env = np.exp(-t * decay) * np.minimum(1.0, t / 0.006)
    out = np.zeros(int(RATE * total))
    i = int(RATE * start)
    out[i:i + len(t)] += (wave_ * env)[: len(out) - i]
    return out


def finish(signal, peak=0.62):
    signal = signal / np.max(np.abs(signal)) * peak
    fade = int(RATE * 0.01)
    signal[-fade:] *= np.linspace(1, 0, fade)
    return signal


def write(path, signal):
    data = (np.clip(signal, -1, 1) * 32767).astype("<i2")
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(data.tobytes())


def main(out_dir):
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    total = 0.62
    # Bonne réponse : deux notes claires qui montent (la5 puis mi6), timbre de clochette.
    correct = note(880.0, 0.16, 0.0, total, decay=14, bell=0.15) + note(1318.5, 0.5, 0.09, total, decay=6.5, bell=0.2)
    write(out / "sfx_correct.wav", finish(correct))
    # Erreur : deux notes graves et douces qui descendent, sans agressivité.
    soft = ((1, 1.0), (2, 0.12))
    wrong = note(311.1, 0.18, 0.0, 0.5, decay=11, partials=soft) + note(233.1, 0.32, 0.13, 0.5, decay=9, partials=soft)
    write(out / "sfx_wrong.wav", finish(wrong, peak=0.5))
    # Paire trouvée : une petite note brève.
    write(out / "sfx_pair.wav", finish(note(1046.5, 0.18, 0.0, 0.2, decay=20, bell=0.1), peak=0.45))
    # Fin de leçon : arpège do-mi-sol-do puis accord tenu.
    total = 1.6
    fanfare = sum(note(f, 0.5, i * 0.11, total, decay=7, bell=0.15) for i, f in enumerate([523.25, 659.25, 783.99, 1046.5]))
    fanfare = fanfare + sum(note(f, 1.1, 0.48, total, decay=3.2, bell=0.1) * 0.6 for f in [523.25, 659.25, 783.99, 1046.5])
    write(out / "sfx_complete.wav", finish(fanfare))


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "app/src/main/res/raw")
