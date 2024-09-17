// Code generated by bavard DO NOT EDIT

package ringsis_64_8

import (
	"testing"

	"github.com/consensys/gnark-crypto/ecc/bls12-377/fr/fft"
	"github.com/consensys/linea-monorepo/prover/maths/field"
	"github.com/stretchr/testify/assert"
)

func TestPartialFFT(t *testing.T) {

	var (
		domain   = fft.NewDomain(64)
		twiddles = PrecomputeTwiddlesCoset(domain.Generator, domain.FrMultiplicativeGen)
	)

	for mask := 0; mask < 4; mask++ {

		var (
			a = vec123456()
			b = vec123456()
		)

		zeroizeWithMask(a, mask)
		zeroizeWithMask(b, mask)

		domain.FFT(a, fft.DIF, fft.OnCoset())
		partialFFT[mask](b, twiddles)
		assert.Equal(t, a, b)
	}

}

func vec123456() []field.Element {
	vec := make([]field.Element, 64)
	for i := range vec {
		vec[i].SetInt64(int64(i))
	}
	return vec
}

func zeroizeWithMask(v []field.Element, mask int) {
	for i := 0; i < 2; i++ {
		if (mask>>i)&1 == 1 {
			continue
		}

		for j := 0; j < 32; j++ {
			v[32*i+j].SetZero()
		}
	}
}
