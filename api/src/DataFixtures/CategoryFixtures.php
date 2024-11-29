<?php

namespace App\DataFixtures;

use App\Entity\Category;
use Doctrine\Bundle\FixturesBundle\Fixture;
use Doctrine\Persistence\ObjectManager;

class CategoryFixtures extends Fixture
{
    public const CATEGORY_REF_PREFIX = 'category_';

    public function load(ObjectManager $manager): void
    {
        $categoriesData = [
            ['id' => 1, 'label' => 'Plat principal'],
            ['id' => 2, 'label' => 'Italien'],
            ['id' => 3, 'label' => 'Entrée'],
            ['id' => 4, 'label' => 'Végétarien'],
            ['id' => 5, 'label' => 'Dessert'],
            ['id' => 6, 'label' => 'Sans gluten'],
            ['id' => 7, 'label' => 'Japonais'],
            ['id' => 8, 'label' => 'Américain'],
        ];

        foreach ($categoriesData as $data) {
            $category = new Category();
            $category->setLabel($data['label']);

            $manager->persist($category);
            $this->addReference(self::CATEGORY_REF_PREFIX . $data['id'], $category);
        }

        $manager->flush();
    }
}
