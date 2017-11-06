<?php
namespace php\crypto;

/**
 * Class Cipher
 * @package php\crypto
 * @packages crypto
 */
class Cipher
{
    /**
     * Cipher constructor.
     * @param string $algorithm
     * @param string $charset
     */
    public function __construct(string $algorithm, string $charset = 'UTF-8')
    {
    }

    /**
     * @param string $data
     * @param CryptoKey $key
     * @return string
     */
    public function encode(string $data, CryptoKey $key): string
    {
    }

    /**
     * @param string $data
     * @param CryptoKey $key
     * @return string
     */
    public function decode(string $data, CryptoKey $key): string
    {
    }
}